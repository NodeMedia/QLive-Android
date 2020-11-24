package xyz.tanwb.airship.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public interface BaseView {

    Context getContext();

    Activity getActivity();

    void showProgress();

    void hideProgress();

    void advance(Class<?> cls, Object... params);

    void advance(String clsName, Object... params);

    void advance(Intent intent);

    void advanceForResult(Class<?> cls, int requestCode, Object... params);

    void advanceForResult(String clsName, int requestCode, Object... params);

    void advanceForResult(Intent intent, int requestCode);

    void exit();

    void exit(boolean isAnim);
}
