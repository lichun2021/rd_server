package com.hawk.activity.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListSplitter {

	public static <T> List<List<T>> splitList(List<T> originalList, int groupSize) {
		List<List<T>> resultList = new ArrayList<>();

		int numGroups = Math.max(1, originalList.size() / groupSize);
		int[] icnt = new int[numGroups];
		int i = 0;
		while (Arrays.stream(icnt).sum() < originalList.size()) {
			icnt[i % numGroups] = icnt[i % numGroups] + 1;
			i++;
		}
		Arrays.sort(icnt);

		int start = 0;
		for (int j = 0; j < numGroups; j++) {
			int end = Math.min(originalList.size(), start + icnt[j]);
			List<T> sublist = new ArrayList<>(originalList.subList(start, end));
			resultList.add(sublist);
			start = end;
		}

		return resultList;
	}

	public static void main(String[] args) {
		List<Integer> servers = new ArrayList<>();
		// Add some sample servers
		for (int i = 1; i <= 3; i++) {
			servers.add(i);
		}

		List<List<Integer>> result = splitList(servers, 5);

		// Print the result
		for (List<Integer> group : result) {
			System.out.println(group);
		}
		System.out.println(Integer.valueOf(5));
	}
}