package com.moya.simplephotoviewer;

import java.io.File;
import java.io.IOException;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.imaging.jpeg.JpegSegmentReader;
import com.drew.lang.ByteArrayReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.iptc.IptcReader;

public class ExifData {

	public String mTag[];

	public static final int ID_ISO = 0;
	public static final int ID_APERTURE = 1;
	public static final int ID_EXPOSURETIME = 2;
	public static final int ID_EXPOSUREPROGRAM = 3;
	public static final int ID_EXPOSUREMODE = 4;
	public static final int ID_EXPOSUREBIASVALUE = 5;
	public static final int ID_WHITEBALANCE = 6;
	public static final int ID_WHITEBALANCEMODE = 7;
	public static final int ID_FLASH = 8;
	public static final int ID_FOCALLENGTH = 9;
	public static final int ID_METERINGMODE = 10;
	public static final int ID_CONTRAST = 11;
	public static final int ID_SATURATION = 12;
	public static final int ID_SHARPNESS = 13;
	public static final int ID_DIGITALZOOMRATIO = 14;
	public static final int ID_WIDTH = 15;
	public static final int ID_HEIGHT = 16;
	public static final int ID_ORIENTATION = 17;
	public static final int ID_DATETIME_ORIGINAL = 18;
	public static final int ID_MAKE = 19;
	public static final int ID_MODEL = 20;
	public static final int EXIF_DATA_COUNT = 21;

	public static final String TAG_ISO = "ISO:";
	public static final String TAG_APERTURE = "F-Number:";
	public static final String TAG_EXPOSURETIME = "Exposure Time:";
	public static final String TAG_EXPOSUREPROGRAM = "Exposure Program:";
	public static final String TAG_EXPOSUREMODE = "Exposure Mode:";
	public static final String TAG_EXPOSUREBIASVALUE = "Exposure Bias Value:";
	public static final String TAG_WHITEBALANCE = "White Balance:";
	public static final String TAG_WHITEBALANCEMODE = "White Balance Mode:";
	public static final String TAG_FLASH = "Flash:";
	public static final String TAG_FOCALLENGTH = "Focal Length:";
	public static final String TAG_METERINGMODE = "Metering Mode:";
	public static final String TAG_CONTRAST = "Contrast:";
	public static final String TAG_SATURATION = "Saturation:";
	public static final String TAG_SHARPNESS = "Sharpness:";
	public static final String TAG_DIGITALZOOMRATIO = "Digital Zoom Ratio:";
	public static final String TAG_WIDTH = "Image Width:";
	public static final String TAG_HEIGHT = "Image Height:";
	public static final String TAG_ORIENTATION = "Orientation:";
	public static final String TAG_DATETIME_ORIGINAL = "Date/Time Original:";
	public static final String TAG_MAKE = "Make:";
	public static final String TAG_MODEL = "Model:";

	public static final String[] mTagTable = { 
		TAG_ISO, 
		TAG_APERTURE, 
		TAG_EXPOSURETIME, 
		TAG_EXPOSUREPROGRAM, 
		TAG_EXPOSUREMODE,
		TAG_EXPOSUREBIASVALUE, 
		TAG_WHITEBALANCE, 
		TAG_WHITEBALANCEMODE, 
		TAG_FLASH, 
		TAG_FOCALLENGTH, 
		TAG_METERINGMODE,
		TAG_CONTRAST, 
		TAG_SATURATION, 
		TAG_SHARPNESS, 
		TAG_DIGITALZOOMRATIO, 
		TAG_WIDTH, TAG_HEIGHT, 
		TAG_ORIENTATION,
		TAG_DATETIME_ORIGINAL, 
		TAG_MAKE, 
		TAG_MODEL };

	public ExifData() {
		mTag = new String[EXIF_DATA_COUNT];
		for (int i = 0; i < mTag.length; ++i) {
			mTag[i] = "";
		}
	}

	public String getDate() {
		String ret = "";

		int length = mTag[ID_DATETIME_ORIGINAL].length();
		if (length <= 0) {
			return "";
		}
		String datetime = mTag[ID_DATETIME_ORIGINAL].substring(TAG_DATETIME_ORIGINAL.length(), length);

		int posi = datetime.lastIndexOf(' ');
		ret = datetime.substring(0, posi + 1);

		return ret;
	}

	public String getTime() {
		String ret = "";
		if (mTag[ID_DATETIME_ORIGINAL].length() <= 0) {
			return "";
		}

		int posi = mTag[ID_DATETIME_ORIGINAL].lastIndexOf(' ');
		ret = mTag[ID_DATETIME_ORIGINAL].substring(posi + 1, mTag[ID_DATETIME_ORIGINAL].length());

		return ret;
	}
	
	static public ExifData getExif(String filepath) {
		ExifData ret = null;
		try {
			File file = new File(filepath);
			JpegSegmentReader segmentReader = new JpegSegmentReader(file);
			byte[] exifSegment = segmentReader.readSegment(JpegSegmentReader.SEGMENT_APP1);
			byte[] iptcSegment = segmentReader.readSegment(JpegSegmentReader.SEGMENT_APPD);
			Metadata metadata = new Metadata();
			if (exifSegment != null)
				new ExifReader().extract(new ByteArrayReader(exifSegment), metadata);
			if (iptcSegment != null)
				new IptcReader().extract(new ByteArrayReader(iptcSegment), metadata);

			// long start = System.currentTimeMillis();
			ret = readExif(metadata);
			// long end = System.currentTimeMillis();
			// Logger.v("measure: " + (end - start));
		} catch (JpegProcessingException e) {
			System.err.println("error 3a: " + e);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("error 3b: " + e);
			e.printStackTrace();			
		}
		return ret;
	}

	
	static private ExifData readExif(Metadata metadata) {

		ExifData ret = new ExifData();

		ExifSubIFDDirectory exifSubIFDDirectory = metadata.getDirectory(ExifSubIFDDirectory.class);
		ExifIFD0Directory exifIFD0Directory = metadata.getDirectory(ExifIFD0Directory.class);

		if (exifSubIFDDirectory != null) {
			ret.mTag[ExifData.ID_ISO] = ExifData.TAG_ISO
					+ exifSubIFDDirectory.getDescription(exifSubIFDDirectory.TAG_ISO_EQUIVALENT);
			ret.mTag[ExifData.ID_APERTURE] = ExifData.TAG_APERTURE
					+ exifSubIFDDirectory.getDescription(exifSubIFDDirectory.TAG_FNUMBER);
			ret.mTag[ExifData.ID_EXPOSURETIME] = ExifData.TAG_EXPOSURETIME
					+ exifSubIFDDirectory.getDescription(exifSubIFDDirectory.TAG_EXPOSURE_TIME);
			ret.mTag[ExifData.ID_EXPOSUREPROGRAM] = ExifData.TAG_EXPOSUREPROGRAM
					+ exifSubIFDDirectory.getDescription(exifSubIFDDirectory.TAG_EXPOSURE_PROGRAM);
			ret.mTag[ExifData.ID_EXPOSUREMODE] = ExifData.TAG_EXPOSUREMODE
					+ exifSubIFDDirectory.getDescription(exifSubIFDDirectory.TAG_EXPOSURE_MODE);
			ret.mTag[ExifData.ID_EXPOSUREBIASVALUE] = ExifData.TAG_EXPOSUREBIASVALUE
					+ exifSubIFDDirectory.getDescription(exifSubIFDDirectory.TAG_EXPOSURE_BIAS);
			ret.mTag[ExifData.ID_WHITEBALANCE] = ExifData.TAG_WHITEBALANCE
					+ exifSubIFDDirectory.getDescription(exifSubIFDDirectory.TAG_WHITE_BALANCE);
			ret.mTag[ExifData.ID_WHITEBALANCEMODE] = ExifData.TAG_WHITEBALANCEMODE
					+ exifSubIFDDirectory.getDescription(exifSubIFDDirectory.TAG_WHITE_BALANCE_MODE);
			ret.mTag[ExifData.ID_FLASH] = ExifData.TAG_FLASH
					+ exifSubIFDDirectory.getDescription(exifSubIFDDirectory.TAG_FLASH);
			ret.mTag[ExifData.ID_FOCALLENGTH] = ExifData.TAG_FOCALLENGTH
					+ exifSubIFDDirectory.getDescription(exifSubIFDDirectory.TAG_FOCAL_LENGTH);
			ret.mTag[ExifData.ID_METERINGMODE] = ExifData.TAG_METERINGMODE
					+ exifSubIFDDirectory.getDescription(exifSubIFDDirectory.TAG_METERING_MODE);
			ret.mTag[ExifData.ID_CONTRAST] = ExifData.TAG_CONTRAST
					+ exifSubIFDDirectory.getDescription(exifSubIFDDirectory.TAG_CONTRAST);
			ret.mTag[ExifData.ID_SATURATION] = ExifData.TAG_SATURATION
					+ exifSubIFDDirectory.getDescription(exifSubIFDDirectory.TAG_SATURATION);
			ret.mTag[ExifData.ID_SHARPNESS] = ExifData.TAG_SHARPNESS
					+ exifSubIFDDirectory.getDescription(exifSubIFDDirectory.TAG_SHARPNESS);
			ret.mTag[ExifData.ID_DIGITALZOOMRATIO] = ExifData.TAG_DIGITALZOOMRATIO
					+ exifSubIFDDirectory.getDescription(exifSubIFDDirectory.TAG_DIGITAL_ZOOM_RATIO);
			ret.mTag[ExifData.ID_WIDTH] = ExifData.TAG_WIDTH
					+ exifSubIFDDirectory.getDescription(exifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH);
			ret.mTag[ExifData.ID_HEIGHT] = ExifData.TAG_HEIGHT
					+ exifSubIFDDirectory.getDescription(exifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT);
		}

		if (exifIFD0Directory != null) {
			ret.mTag[ExifData.ID_ORIENTATION] = ExifData.TAG_ORIENTATION
					+ exifIFD0Directory.getDescription(exifIFD0Directory.TAG_ORIENTATION);
			ret.mTag[ExifData.ID_DATETIME_ORIGINAL] = ExifData.TAG_DATETIME_ORIGINAL
					+ exifSubIFDDirectory.getDescription(exifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
			ret.mTag[ExifData.ID_MAKE] = ExifData.TAG_MAKE + exifIFD0Directory.getDescription(exifIFD0Directory.TAG_MAKE);
			ret.mTag[ExifData.ID_MODEL] = ExifData.TAG_MODEL + exifIFD0Directory.getDescription(exifIFD0Directory.TAG_MODEL);
		}

		return ret;
	}
}
