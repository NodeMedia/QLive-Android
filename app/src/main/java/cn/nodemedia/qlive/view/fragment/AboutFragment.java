package cn.nodemedia.qlive.view.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.nodemedia.qlive.R;

/**
 * Created by aliang on 2017/12/4.
 */

public class AboutFragment extends Fragment implements View.OnClickListener {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView w = view.findViewById(R.id.website);
        TextView a = view.findViewById(R.id.android_sdk);
        TextView i = view.findViewById(R.id.ios_sdk);
        TextView s = view.findViewById(R.id.node_media_server);

        w.setOnClickListener(this);
        a.setOnClickListener(this);
        i.setOnClickListener(this);
        s.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.website: {
                Uri uri = Uri.parse("http://www.nodemedia.cn");
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }

            break;
            case R.id.android_sdk: {
                Uri uri = Uri.parse("https://github.com/NodeMedia/NodeMediaClient-Android");
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
            break;
            case R.id.ios_sdk: {
                Uri uri = Uri.parse("https://github.com/NodeMedia/NodeMediaClient-iOS");
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
            break;
            case R.id.node_media_server: {
                Uri uri = Uri.parse("https://github.com/illuspas/Node-Media-Server");
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
            break;
        }
    }
}
