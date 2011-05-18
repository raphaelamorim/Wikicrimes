package org.wikicrimes.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Util {

	public static double roundWithDecimals(double num, int casasDecimais) {
		int temp = (int) ((num * Math.pow(10, casasDecimais)));
		return (((double) temp) / Math.pow(10, casasDecimais));
	}

	public static double milisToSecs(long milis, int casasDecimais) {
		double seg = (double) milis / 1000;
		return roundWithDecimals(seg, casasDecimais);
	}

	/**
	 * Limita o "valor" entre "min" e "max". Se o valor for menor q min, retorna
	 * min. Se for maior q max, retorna max. Caso contrario retorna "valor".
	 */
	public static int limit(int valor, int min, int max) {
		return Math.min(max, Math.max(min, valor));
	}

	/**
	 * Limita o "valor" entre "min" e "max". Se o valor for menor q min, retorna
	 * min. Se for maior q max, retorna max. Caso contrario retorna "valor".
	 */
	public static double limit(double valor, double min, double max) {
		return Math.min(max, Math.max(min, valor));
	}

	public static <T, N extends Number> List<T> sortKeys(List<T> keys,
			List<N> values) {
		int n = keys.size();
		if (values.size() != n)
			throw new InvalidParameterException();
		List<KeyValue<T, N>> pairs = new ArrayList<KeyValue<T, N>>();
		for (int i = 0; i < n; i++) {
			pairs.add(new KeyValue<T, N>(keys.get(i), values.get(i)));
		}
		Collections.sort(pairs);
		List<T> sortedKeys = new ArrayList<T>();
		for (int i = 0; i < n; i++) {
			sortedKeys.add(pairs.get(i).key);
		}
		return sortedKeys;
	}

	private static class KeyValue<T, N extends Number> implements
			Comparable<KeyValue<T, N>> {

		T key;
		N value;

		public KeyValue(T key, N value) {
			super();
			this.key = key;
			this.value = value;
		}

		public int compareTo(KeyValue<T, N> o) {
			double diff = value.doubleValue() - o.value.doubleValue();
			if (diff == 0)
				return 0;
			else if (diff > 0)
				return 1;
			else
				return -1;
		}
	}

	public static int sum(int[] values) {
		int sum = 0;
		for (int v : values) {
			sum += v;
		}
		return sum;
	}

	public static double sum(double[] values) {
		double sum = 0;
		for (double v : values) {
			sum += v;
		}
		return sum;
	}

	public static int sum(Collection<Integer> values) {
		return sum(toIntArray(values));
	}

	public static double sumDoubles(Collection<Double> values) {
		return sum(toDoubleArray(values));
	}

	public static int[] toIntArray(List<Integer> list) {
		int n = list.size();
		int[] array = new int[n];
		for (int i = 0; i < n; i++) {
			array[i] = list.get(i);
		}
		return array;
	}

	public static int[] toIntArray(Collection<Integer> collection) {
		return toIntArray(new ArrayList<Integer>(collection));
	}

	public static double[] toDoubleArray(List<Double> list) {
		int n = list.size();
		double[] array = new double[n];
		for (int i = 0; i < n; i++) {
			array[i] = list.get(i);
		}
		return array;
	}

	public static double[] toDoubleArray(Collection<Double> collection) {
		return toDoubleArray(new ArrayList<Double>(collection));
	}

	public static int nullToZero(Integer i) {
		return i == null ? 0 : i;
	}

	public static String zip(String stream) {
		ByteArrayInputStream fis = null;
		ByteArrayOutputStream fos = null;
		String erg = "";
		try {
			fis = new ByteArrayInputStream(stream.getBytes("ISO-8859-1"));
			fos = new ByteArrayOutputStream();

			ZipOutputStream zos = new ZipOutputStream(fos);
			ZipEntry ze = new ZipEntry("name1");
			zos.putNextEntry(ze);
			final int BUFSIZ = 4096;
			byte inbuf[] = new byte[BUFSIZ];
			int n;
			while ((n = fis.read(inbuf)) != -1)
				zos.write(inbuf, 0, n);
			fis.close();
			fis = null;
			zos.close();
		} catch (IOException e) {
			System.err.println(e);
			e.printStackTrace();
		} finally {
			try {
				if (fis != null)
					fis.close();
				if (fos != null) {
					fos.close();
					erg = new String(fos.toByteArray(), "ISO-8859-1");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return erg;
	}

	public static String unzip(String stream) {
		ByteArrayInputStream fis = null;
		ByteArrayOutputStream fos = null;
		String erg = "";
		try {
			fis = new ByteArrayInputStream(stream.getBytes("ISO-8859-1"));
			fos = new ByteArrayOutputStream();
			ZipInputStream zis = new ZipInputStream(fis);
			final int BUFSIZ = 4096;
			byte inbuf[] = new byte[BUFSIZ];
			int n;
			while ((n = zis.read(inbuf, 0, BUFSIZ)) != -1) {
				System.out.println(".");
				fos.write(inbuf, 0, n);
			}
			zis.close();
			fis = null;
			fos.close();
		} catch (IOException e) {
			System.err.println(e);
			e.printStackTrace();
		} finally {
			try {
				if (fis != null)
					fis.close();
				if (fos != null)
					fos.close();
				erg = new String(fos.toByteArray(), "ISO-8859-1");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return erg;
	}

	/*public static String gzip(String str) {
		try {
			String encoding = "ISO-8859-1";
			byte[] bytes = str.getBytes(encoding);
			ByteArrayOutputStream out = new ByteArrayOutputStream(bytes.length);
			GZIPOutputStream zout = new GZIPOutputStream(out);
			zout.write(bytes);
			zout.flush();
			return new String(out.toByteArray(), encoding);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String ungzip(String str) {
		try {
			String encoding = "ISO-8859-1";
			GZIPInputStream zin = new GZIPInputStream(new ByteArrayInputStream(str.getBytes(encoding)));
			byte[] bytes = new byte[zin.available()];
			zin.read(bytes);
			return new String(bytes, encoding);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}*/
	
}
