#!/bin/sh
# һ��������ύ������������svnĿ¼
#�˽ű�������´��룬Ҳ�����Engine�е���Ŀ���д��������ֱ��ʹ��Engine/Publish�ļ����µ�jar��������Ŀ����
# ���ж�Engine����Ŀ��������޸ģ���������Engine�е�build.bat���д����Ȼ�������д˽ű�
PUBLISH_DIR=Publish
SVN_XML_DIR=xmlconfig

svn checkout -q svn://10.0.1.254/redalert/Version_Server/trunk/server-start %PUBLISH_DIR%
gradle clean
gradle jar
# ��������-DversionFolder=windows�� windows �ַ���Ϊ��汾����ѡ���������Ӧ���ļ�Ŀ¼��Launcher�ļ����µİ汾�ļ������ƣ�Ĭ���ļ�������Ϊ windows
 gradle -DversionFolder=windows publish

 if [ ! -d $SVN_XML_DIR ] ;then
	rm -rf $SVN_XML_DIR
  fi
svn checkout -q svn://10.0.1.254/redalert/Resource_Product/trunk/0.���ñ�/2.��̨ $SVN_XML_DIR
cp -rf $SVN_XML_DIR/xml $PUBLISH_DIR/xml/
cp -rf $SVN_XML_DIR/activity $PUBLISH_DIR/activity/
echo commit SVN
cd $PUBLISH_DIR
svn add * --force
svn commit -m �������Զ�����ύ
cd ..
echo delete publish dir
rm -rf $PUBLISH_DIR
rm -rf $SVN_XML_DIR
echo =====================================
echo ============build success============
echo =====================================
pause