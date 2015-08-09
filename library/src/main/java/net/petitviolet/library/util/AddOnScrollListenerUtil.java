package net.petitviolet.library.util;

import android.widget.AbsListView;

import java.lang.reflect.Field;

public class AddOnScrollListenerUtil {
    private static final String TAG = AddOnScrollListenerUtil.class.getSimpleName();

    /**
     * AbsListViewにonScrollListenerを追加する
     * @param listView
     * @param onScrollListener
     */
    public static void addListener(AbsListView listView, final AbsListView.OnScrollListener onScrollListener) {
        AbsListView.OnScrollListener originalOnScrollListener = null;
        Class<?> listViewClass = listView.getClass();
        try {
            while (listViewClass != AbsListView.class) {
                listViewClass = listViewClass.getSuperclass();
            }
            // field名は決め打ち
            Field field = listViewClass.getDeclaredField("mOnScrollListener");
            field.setAccessible(true);
            originalOnScrollListener = (AbsListView.OnScrollListener) field.get(listView);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        final AbsListView.OnScrollListener defaultOnScrollListener = originalOnScrollListener;

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (defaultOnScrollListener != null) {
                    defaultOnScrollListener.onScrollStateChanged(view, scrollState);
                }
                onScrollListener.onScrollStateChanged(view, scrollState);
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (defaultOnScrollListener != null) {
                    defaultOnScrollListener.onScroll(absListView, firstVisibleItem, visibleItemCount, totalItemCount);
                }
                onScrollListener.onScroll(absListView, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        });
    }
}