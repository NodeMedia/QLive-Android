package cn.nodemedia.qlive.view.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import androidx.annotation.Nullable;
import android.view.View;

import cn.nodemedia.qlive.R;
import cn.nodemedia.qlive.view.PlayActivity;
import xyz.tanwb.airship.utils.Log;

public class PlayFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    private SharedPreferences sp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
        addPreferencesFromResource(R.xml.pref_play);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 得到我们的存储Preferences值的对象，然后对其进行相应操作
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        initPreference("play_stream_url", "rtmp://", true);
        initPreference("buffertime", "300", true);
        initPreference("maxbuffertime", "1000", true);
        initPreference("video_scale_mode", "1", true);
        initPreference("decode_auto_hardware_acceleration", "true", true);
        initPreference("rtsp_transport", "tcp", true);
        initPreference("play_cryptokey", "", true);

        findPreference("play_start").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), PlayActivity.class));
                return true;
            }
        });
    }

    private void initPreference(String key, String defValue, boolean isListener) {
        Preference preference = findPreference(key);
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            String stringValue = sp.getString(key, defValue);
            setListSummary(listPreference, stringValue);
        } else if (preference instanceof SwitchPreference) {
            SwitchPreference switchPreference = (SwitchPreference) preference;
            boolean defBooleanValue = Boolean.parseBoolean(defValue);
            boolean booleanValue = sp.getBoolean(key, defBooleanValue);
            setSwitchChecked(switchPreference, booleanValue);
        } else if (preference instanceof EditTextPreference) {
            EditTextPreference editTextPreference = (EditTextPreference) preference;
            String stringValue = sp.getString(key, defValue);
            setEditSummary(editTextPreference, stringValue);
        }
        if (isListener) {
            preference.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        Log.e("preference.getKey():" + preference.getKey() + "  value:" + value);
        String stringValue = value.toString();
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            setListSummary(listPreference, stringValue);
        } else if (preference instanceof EditTextPreference) {
            preference.setSummary(stringValue);
        }
        return true;
    }

    private void setListSummary(ListPreference listPreference, String stringValue) {
        int index = listPreference.findIndexOfValue(stringValue);
        listPreference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
    }

    private void setSwitchChecked(SwitchPreference switchPreference, boolean booleanValue) {
        switchPreference.setChecked(booleanValue);
    }

    private void setEditSummary(EditTextPreference editTextPreference, String stringValue) {
        editTextPreference.setSummary(stringValue);
    }
}
