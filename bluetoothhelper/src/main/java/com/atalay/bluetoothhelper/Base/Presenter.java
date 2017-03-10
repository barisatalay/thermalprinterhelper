package com.atalay.bluetoothhelper.Base;

/**
 * Created by barisatalay on 02.09.2016.
 */
public interface Presenter<V extends BaseView> {
    void attachView(V mvpView);

    void detachView();
}
