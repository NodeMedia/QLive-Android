package xyz.tanwb.airship.view.contract;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;

import xyz.tanwb.airship.R;
import xyz.tanwb.airship.view.BasePresenter;
import xyz.tanwb.airship.view.BaseView;

public abstract class PasswordPresenter<T extends BaseView> extends BasePresenter<T> {

    private boolean isSetTextChanged;

    public void switchPasswordEye(EditText password, ImageView passwordEye) {
        if (password.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)) {
            password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordEye.setImageResource(R.drawable.icon_eye_hide);
        } else {
            password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordEye.setImageResource(R.drawable.icon_eye_show);
        }

        if (!isSetTextChanged) {
            isSetTextChanged = true;
            password.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable edt) {
                    try {
                        String temp = edt.toString();
                        String tem = temp.substring(temp.length() - 1, temp.length());
                        char[] temC = tem.toCharArray();
                        int mid = temC[0];
                        if (mid >= 48 && mid <= 57) {
                            return;
                        }
                        if (mid >= 65 && mid <= 90) {
                            return;
                        }
                        if (mid > 97 && mid <= 122) {
                            return;
                        }
                        if (mid == '_') {
                            return;
                        }
                        edt.delete(temp.length() - 1, temp.length());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
