import json
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent
PAY_XML = ROOT / "GameServer" / "xml" / "pay.xml"
PAY_GIFT_XML = ROOT / "GameServer" / "xml" / "payGift.xml"


PAY_NAME_OVERRIDES = {
    # pay.xml overrides
    "com.tencent.tmgp.hjol.diamond_60": "购买金条6元",
    "com.tencent.tmgp.hjol.diamond_300": "购买金条30元",
    "com.tencent.tmgp.hjol.diamond_680": "购买金条68元",
    "com.tencent.tmgp.hjol.diamond_1280": "购买金条128元",
    "com.tencent.tmgp.hjol.diamond_3280": "购买金条328元",
    "com.tencent.tmgp.hjol.diamond_6480": "购买金条648元",
    # payGift main few overrides (已有 nameData，但留作兜底)
    "com.tencent.tmgp.hjol.gift_000": "0元礼包",
    "com.tencent.tmgp.hjol.gift_100": "1元礼包",
    "com.tencent.tmgp.hjol.gift_300": "3元礼包",
    "com.tencent.tmgp.hjol.gift_600": "6元礼包",
    "com.tencent.tmgp.hjol.gift_1200": "12元礼包",
}


def parse_xml(path: Path, default_recharge_type: int, is_gift: bool = False):
    """Parse pay or payGift xml to a dict keyed by saleId."""
    data = {}
    order = []
    tree = ET.parse(path)
    for node in tree.getroot().iter("data"):
        sale_id = node.attrib.get("saleId")
        if not sale_id:
            continue
        order.append(sale_id)

        entry = data.setdefault(sale_id, {})

        # price: payRMB is in cents, convert to yuan
        pay_rmb = node.attrib.get("payRMB")
        price = round(int(pay_rmb) / 100, 2) if pay_rmb else None

        name = (
            node.attrib.get("nameData")
            or node.attrib.get("desData")
            or PAY_NAME_OVERRIDES.get(sale_id)
            or sale_id
        )
        channel = node.attrib.get("channelType", "").lower()

        # For pay.xml (no channel), treat as both id/andid when present.
        if channel == "ios" or not is_gift:
            if node.attrib.get("id"):
                entry["id"] = node.attrib.get("id")
        if channel == "android" or not is_gift:
            if node.attrib.get("id"):
                entry["andid"] = node.attrib.get("id")

        entry["name"] = name
        entry["rechargeType"] = default_recharge_type
        if price is not None:
            entry["price"] = price
    return data, order


def merge_pay_and_gift():
    result = {}
    order = []
    # pay.xml (rechargeType=1)
    pay_data, pay_order = parse_xml(PAY_XML, default_recharge_type=1, is_gift=False)
    order.extend(pay_order)
    for k, v in pay_data.items():
        result[k] = v
    # payGift.xml (rechargeType=2 per requirement)
    gift_data, gift_order = parse_xml(PAY_GIFT_XML, default_recharge_type=2, is_gift=True)
    order.extend(gift_order)
    for k, v in gift_data.items():
        if k not in result:
            result[k] = v
        else:
            # merge missing fields
            for kk, vv in v.items():
                if vv:
                    result[k][kk] = vv
    # order: pay.xml first (原顺序)，然后其余按键名排序但不重复
    seen = set()
    ordered_keys = []
    for k in order:
        if k not in seen:
            ordered_keys.append(k)
            seen.add(k)
    for k in sorted(result.keys()):
        if k not in seen:
            ordered_keys.append(k)
            seen.add(k)
    return result, ordered_keys


def main():
    result, ordered_keys = merge_pay_and_gift()
    ordered = {k: result[k] for k in ordered_keys}
    print(json.dumps(ordered, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()

