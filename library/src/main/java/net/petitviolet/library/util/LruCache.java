package net.petitviolet.library.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * リクエストした結果をディスク/メモリにキャッシュするシングルトン
 */
public class LruCache {
    private static final String TAG = LruCache.class.getSimpleName();
    private static final String DIR_NAME = "petitviolet";
    private static final int MEM_CACHE_SIZE = 1024 * 1024 * 5;
    private final android.support.v4.util.LruCache mLruCache;
    private static DiskLruCache mDiskLruCache;
    private static LruCache instance = new LruCache();

    private LruCache() {
        this.mLruCache = new android.support.v4.util.LruCache(MEM_CACHE_SIZE);
    }

    private void openDiskCache(Context context) {
        if (mDiskLruCache == null) {
            mDiskLruCache = DiskLruCache.openCache(context);
        }
    }

    public static LruCache getInstance(Context context) {
        if (context != null) {
            instance.openDiskCache(context);
        }
        return instance;
    }

    /**
     * HTTPキャッシュ出来るようにする
     *
     * @param context
     */
    public void enableHttpResponseCache(Context context) {
        try {
            File httpCacheDir = new File(context.getCacheDir(), DIR_NAME);
            Class.forName("android.net.http.HttpResponseCache")
                    .getMethod("install", File.class, long.class)
                    .invoke(null, httpCacheDir, MEM_CACHE_SIZE);
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    /**
     * ディスク/メモリ両方cacheをclear
     */
    public void clearAllCache() {
        mLruCache.evictAll();
        mDiskLruCache.clearCache();
    }

    /**
     * URLな文字列ををURLエンコードする
     *
     * @param url
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String urlEncoder(URL url) throws UnsupportedEncodingException {
        return URLEncoder.encode(String.valueOf(url), "UTF-8");
    }

    private static String urlEncoder(String url) throws UnsupportedEncodingException {
        return URLEncoder.encode(url, "UTF-8");
    }

    /**
     * メモリにキャッシュする
     *
     * @param key  メモリからキャッシュをwriteするためのkey
     * @param item キャッシュしたいObject
     */
    public void putMemory(String key, Object item) {
        try {
            mLruCache.put(urlEncoder(key), item);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * メモリにキャッシュする
     *
     * @param url
     * @param item
     */
    public void putMemory(URL url, Object item) {
        putMemory(String.valueOf(url), item);
    }

    /**
     * メモリからObjectをreadする
     *
     * @param key メモリキャッシュに対するObjectのkey
     * @return キャッシュが存在すればそのObject, なければnull
     */
    public Object getMemory(String key) {
        try {
            synchronized (mLruCache) {
                return mLruCache.get(urlEncoder(key));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * メモリからObjectをreadする
     *
     * @param url
     * @return
     */
    public Object getMemory(URL url) {
        return getMemory(String.valueOf(url));
    }

    /**
     * ディスクにキャッシュする
     *
     * @param key  ディスクキャッシュするファイル名となる文字列
     * @param item キャッシュしたいbyte列
     * @return 成功したかどうか
     */
    public boolean putDisk(String key, byte[] item) {
        if (mDiskLruCache.containsKey(key)) {
            return true;
        }
        try {
            mDiskLruCache.put(urlEncoder(key), item);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ディスクにキャッシュする
     *
     * @param url
     * @param item
     * @return
     */
    public boolean putDisk(URL url, byte[] item) {
        return putDisk(String.valueOf(url), item);
    }

    /**
     * ディスクからキャッシュをreadする
     *
     * @param key キャッシュが入っているファイル名と対応するkey
     * @return byte[]
     */
    public byte[] getDisk(String key) {
        try {
            synchronized (mDiskLruCache) {
                return mDiskLruCache.get(urlEncoder(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isDiskCacheExist(String key) {
        try {
            return mDiskLruCache.containsKey(urlEncoder(key));
        } catch (NullPointerException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isDiskCacheExist(URL url) {
        return isDiskCacheExist(String.valueOf(url));
    }

    /**
     * ディスクからキャッシュをreadする
     *
     * @param url
     * @return
     */
    public byte[] getDisk(URL url) {
        long start = System.currentTimeMillis();
        byte[] result = getDisk(String.valueOf(url));
        long end = System.currentTimeMillis();
        Log.d(TAG, "getDisk end - start:" + (end - start) + ":" + url);
        return result;
    }

    /**
     * byteをキャッシュしていたファイル名を取得する
     *
     * @param key キャッシュファイル名に対応するkey
     * @return Fileオブジェクト
     */
    public File getDiskCacheFile(String key) {
        try {
            return mDiskLruCache.getCacheFile(urlEncoder(key));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * ディスクキャッシュ用class
     */
    private static class DiskLruCache {
        private static final String TAG = DiskLruCache.class.getSimpleName();
        private static final int INITIAL_CAPACITY = 16;
        private static final float LOAD_FACTOR = 0.75f;
        private static final String CACHE_FILENAME_PREFIX = "my_cache_";
        private static final int IO_BUFFER_SIZE = 3 * 1024; // 3KB
        private static final int MAX_REMOVALS = 4;
        private static final int DISK_MAX_CACHE_ITEM_COUNT = 32;
        private static final long DISK_MAX_CACHE_BYTE_SIZE = 1024 * 1024 * 10;  // 10MB

        private final File mCacheDir;
        private int mCacheSize = 0;
        private int mCacheByteSize = 0;
        private static DiskLruCache instance;

        /**
         * LinkedHashMapはLRUに適したデータ構造
         * HashMapとLinkedListの両方持ち、Hashアクセスと挿入順序保存
         */
        private final Map<String, String> mLinkedHashMap =
                Collections.synchronizedMap(
                        new LinkedHashMap<String, String>(INITIAL_CAPACITY, LOAD_FACTOR, true)
                );

        /**
         * ディスクキャッシュ用意
         * ディレクトリ作成
         *
         * @param context
         * @return
         */
        public static DiskLruCache openCache(Context context) {
            if (instance == null) {
                File cacheDir = DiskLruCache.getDiskCacheDir(context, DIR_NAME);
                if (!cacheDir.exists()) {
                    cacheDir.mkdir();
                }
                instance = new DiskLruCache(cacheDir);
            }
            return instance;
        }

        /**
         * コンストラクタはprivate
         *
         * @param cacheDir
         * @see DiskLruCache#openDiskCache(Context)
         */
        private DiskLruCache(File cacheDir) {
            mCacheDir = cacheDir;
        }

        /**
         * ディスクにキャッシュ
         *
         * @param key  キャッシュするbyte[]の識別文字列
         * @param data byte列
         */
        public void put(String key, byte[] data) {
            synchronized (mLinkedHashMap) {
                if (mLinkedHashMap.get(key) == null) {
                    final String file = createFilePath(mCacheDir, key);
                    Log.d(TAG, file);
                    writeItemToFile(file, data);
                    update(key, file);
                    if (isCachable(data)) {
                        // キャッシュに入れるにはでかすぎるものは次回削除
                        flushCache();
                    }
                }
            }
        }

        /**
         * ディスクキャッシュ可能なサイズかどうか
         *
         * @param data
         * @return
         */
        public boolean isCachable(byte[] data) {
            return data.length < DISK_MAX_CACHE_BYTE_SIZE;
        }

        /**
         * ファイルにbyte[]を書き込む
         *
         * @param file ファイルのパス文字列
         * @param item byte[]
         */
        private void writeItemToFile(String file, byte[] item) {
            OutputStream out = null;
            try {
                out = new BufferedOutputStream(new FileOutputStream(file), IO_BUFFER_SIZE);
                out.write(item);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * キャッシュの管理機構をアップデート
         *
         * @param key
         * @param file
         */
        private void update(String key, String file) {
            mLinkedHashMap.put(key, file);
            mCacheSize = mLinkedHashMap.size();
            mCacheByteSize += new File(file).length();
        }

        /**
         * キャッシュのサイズ管理
         * キャッシュしているアイテムの数orサイズが超過していたりすると削除
         */
        private void flushCache() {
            Entry<String, String> eldestEntry;
            File eldestFile;
            long eldestFileSize;
            int count = 0;

            // 古いやつから消していく
            while (count < MAX_REMOVALS && (mCacheSize > DISK_MAX_CACHE_ITEM_COUNT || mCacheByteSize > DISK_MAX_CACHE_BYTE_SIZE)) {
                eldestEntry = mLinkedHashMap.entrySet().iterator().next();
                eldestFile = new File(eldestEntry.getValue());
                eldestFileSize = eldestFile.length();
                mLinkedHashMap.remove(eldestEntry.getKey());
                eldestFile.delete();
                mCacheSize = mLinkedHashMap.size();
                mCacheByteSize -= eldestFileSize;
                count++;
                Log.d(TAG, "flushCache - Removed cache file, " + eldestFile + ", " + eldestFileSize);
            }
        }

        /**
         * キャッシュをディスクから読み出す
         *
         * @param key 識別子
         * @return byte[]
         * @throws Exception
         */
        public byte[] get(String key) throws Exception {
            synchronized (mLinkedHashMap) {
                String file = mLinkedHashMap.get(key);
                if (file != null) {
                    Log.d(TAG, "Disk cache hit");
                    return readFileToByte(file);
                } else {
                    file = createFilePath(mCacheDir, key);
                    if (new File(file).exists()) {
                        update(key, file);
                        Log.d(TAG, "Disk cache hit (existing file)");
                        return readFileToByte(file);
                    }
                }
                return null;
            }
        }

        /**
         * 識別子に対応したFileオブジェクトを取得
         *
         * @param key
         * @return
         */
        public File getCacheFile(String key) {
            synchronized (mLinkedHashMap) {
                String file = mLinkedHashMap.get(key);
                if (file == null) {
                    file = createFilePath(mCacheDir, key);
                }
                return new File(file);
            }
        }

        /**
         * ファイルからbyte[]を読み出す
         *
         * @param filePath ファイルパス文字列
         * @return byte[]
         * @throws Exception
         */
        private byte[] readFileToByte(String filePath) throws Exception {
            byte[] bytes = new byte[IO_BUFFER_SIZE];
            FileInputStream fis = new FileInputStream(filePath);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int current;
            while ((current = fis.read(bytes)) != -1) {
                baos.write(bytes, 0, current);
            }
            baos.close();
            fis.close();
            bytes = baos.toByteArray();
            Log.d(TAG, "read from file bytes:" + filePath);
            return bytes;
        }

        /**
         * 識別子に対応したキャッシュが存在するかどうか
         * 存在しなくても
         *
         * @param key
         * @return
         */
        public boolean containsKey(String key) {
            if (mLinkedHashMap.containsKey(key)) {
                return true;
            }
            final String existingFile = createFilePath(mCacheDir, key);
            if (new File(existingFile).exists()) {
                update(key, existingFile);
                return true;
            }
            return false;
        }

        /**
         * キャッシュを全て削除
         */
        public void clearCache() {
            clearCache(mCacheDir);
            mLinkedHashMap.clear();
            mCacheByteSize = 0;
            mCacheSize = 0;
        }

        /**
         * 特定のキャッシュを削除
         *
         * @param context
         * @param uniqueName
         */
        public void clearCache(Context context, String uniqueName) {
            File cacheDir = getDiskCacheDir(context, uniqueName);
            clearCache(cacheDir);
        }

        /**
         * キャッシュファイルがこのクラスで管理しているファイルかどうかのFilter
         */
        private static final FilenameFilter cacheFileFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.startsWith(CACHE_FILENAME_PREFIX);
            }
        };

        /**
         * このクラスで管理するキャッシュを削除する
         *
         * @param cacheDir
         */
        private static void clearCache(File cacheDir) {
            final File[] files = cacheDir.listFiles(cacheFileFilter);
            for (File file : files) {
                file.delete();
                Log.d(TAG, file.toString() + " is deleted");
            }
        }

        /**
         * キャッシュを格納するディレクトリを取得
         * 外部ストレージ(非SDカード)が使用可能なら外部キャッシュディレクトリ
         * そうでないなら内部キャッシュディレクトリ
         *
         * @param context
         * @param uniqueName
         * @return
         */
        public static File getDiskCacheDir(Context context, String uniqueName) {
            final String cachePath = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                    || isExternalStorageRemovable() ? context.getCacheDir().getPath() : getExternalCacheDir(context).getPath();

            return new File(cachePath + File.separator + uniqueName);
        }

        /**
         * 外部ストレージが取り外し出来るSDカードかどうか
         *
         * @return SDカードならtrue
         */
        protected static boolean isExternalStorageRemovable() {
            return Environment.isExternalStorageRemovable();
        }

        /**
         * 外部キャッシュディレクトリのFileオブジェクト
         *
         * @param context
         * @return
         */
        protected static File getExternalCacheDir(Context context) {
            File cacheDir = context.getExternalCacheDir();
            if (cacheDir != null) {
                return cacheDir;
            }

            String cacheDirStr = "/Android/data/" + context.getPackageName() + "/cache/";
            return new File(Environment.getExternalStorageDirectory().getPath() + cacheDirStr);
        }


        /**
         * キャッシュ用ファイルを作成する
         *
         * @param cacheDir
         * @param key
         * @return ファイルパス文字列
         */
        public String createFilePath(File cacheDir, String key) {
            return cacheDir.getAbsolutePath() + File.separator + CACHE_FILENAME_PREFIX + key;
        }

    }

}
