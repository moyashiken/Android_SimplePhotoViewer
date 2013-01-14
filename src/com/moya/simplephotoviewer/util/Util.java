package com.moya.simplephotoviewer.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeMap;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class Util {

	// get image file count
	static public int getImageFileCount(String folderPath) {
		return getFileList(folderPath).size();
	}

	// get descending order image file list
	static public ArrayList<String> getFileListDes(String folderPath) {

		ArrayList<String> ret = new ArrayList<String>();

		ArrayList<File> fileList = getFileList(folderPath);

		// descending sort file name
		TreeMap<String, File> map = new TreeMap<String, File>();
		for (File file : fileList) {
			map.put(file.getPath(), file);
		}

		NavigableSet<String> mapDesList = map.descendingKeySet();
		for (String file : mapDesList) {
			ret.add(file);
		}

		return ret;
	}

	// get image file list
	static private ArrayList<File> getFileList(String folderPath) {

		ArrayList<File> fileList = new ArrayList<File>();
		File folder = new File(folderPath);
		if (!folder.exists()) {
			return fileList;
		}

		File[] files = new File(folderPath).listFiles();

		for (File file : files) {
			if (file.isFile()) {
				String ext = getSuffix(file.getName());
				if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("png")) {
					fileList.add(file);
				}
			}
		}
		return fileList;
	}
	static public String getDirectory(String filepath) {
		if (filepath == null)
			return null;
		int point = filepath.lastIndexOf("/");
		if (point != -1) {
			return filepath.substring(0, point);
		}
		return filepath;
	}	
	
	static public String getFilename(String filepath) {
		if (filepath == null)
			return null;
		int point = filepath.lastIndexOf("/");
		if (point != -1) {
			return filepath.substring(point+1, filepath.length());
		}
		return filepath;
	}	
	
	static public String getSuffix(String fileName) {
		if (fileName == null)
			return null;
		int point = fileName.lastIndexOf(".");
		if (point != -1) {
			return fileName.substring(point + 1);
		}
		return fileName;
	}
	


	static public ArrayList<String> getFileNameList(ArrayList<String> fullpathlist) {
		ArrayList<String> ret = new ArrayList<String>();

		for (String fullpath : fullpathlist) {
			int slash_posi = fullpath.lastIndexOf("/");
			ret.add(fullpath.substring(slash_posi + 1, fullpath.length()));
		}

		return ret;
	}

	static public int getNumber(String dir, String filename) {

		ArrayList<String> fullpath_list = getFileListDes(dir);
		ArrayList<String> filename_list = getFileNameList( fullpath_list );
		int cnt = 0;
		for( String itFilename : filename_list ){
			
			if(itFilename.equals(filename)){
				return cnt;
			}
			++cnt;
		}
		
		return cnt;
	}
	
	static public int getNumber(String filepath) {

		String dir = getDirectory(filepath);
		String filename = getFilename(filepath);
		
		ArrayList<String> fullpath_list = getFileListDes(dir);
		ArrayList<String> filename_list = getFileNameList( fullpath_list );
		int cnt = 0;
		for( String itFilename : filename_list ){
			
			if(itFilename.equals(filename)){
				return cnt;
			}
			++cnt;
		}
		
		return cnt;
	}	
	
	static public String getFilePathFromNumber(String dir, int number ){
		ArrayList<String> list = getFileListDes(dir);
		return list.get(number);
	}
	
	static public String getNextFile(String filepath){
		String dir = getDirectory(filepath);
		int n = getNumber(filepath);
		
		if( n <= 0){
			return null;
		}
		
		return getFilePathFromNumber(dir, n - 1);
	}
	
	static public String getPreviousFile(String filepath){
		String dir = getDirectory(filepath);
		int n = getNumber(filepath);
		int size = getImageFileCount(dir);
		
		if( size -1 <= n){
			return null;
		}
		
		return getFilePathFromNumber(dir, n + 1);
	}
	
}
