package xyz.tanwb.airship.utils;

import android.content.Context;
import android.graphics.Typeface;
import androidx.core.view.LayoutInflaterFactory;
import androidx.appcompat.app.AppCompatDelegate;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * 修改App字体
 * <p>
 * <pre>
 * protected void onCreate(Bundle savedInstanceState) {
 * LayoutInflaterCompat.setFactory(getLayoutInflater(),
 * new IconFontLayoutFactory(this,getDelegate()));
 * super.onCreate(savedInstanceState);
 * setContentView(R.layout.activity_main);
 * }
 * </pre>
 * <pre>
 * FontHelper.injectFont(findViewById(android.R.id.content));
 *
 * Class
 *
 * public static final String DEF_FONT = "fonts/ocnyangfont.ttf";
 *
 * public static final void injectFont(View rootView) {
 * injectFont(rootView, Typeface.createFromAsset(rootView.getContext().getAssets(),
 * DEF_FONT));
 * }
 *
 * private static void injectFont(View rootView, Typeface typeface) {
 * if (rootView instanceof ViewGroup) {
 * ViewGroup viewGroup = (ViewGroup) rootView;
 * int childViewCount = viewGroup.getChildCount();
 * for (int i = 0; i < childViewCount; i++) {
 * injectFont(viewGroup.getChildAt(i), typeface);
 * }
 * } else if (rootView instanceof TextView) {
 * ((TextView) rootView).setTypeface(typeface);
 * }
 * }
 * </pre>
 * </p>
 */
public class FontUtils implements LayoutInflaterFactory {

    private static Typeface sTypeface;

    private AppCompatDelegate appCompatDelegate;

    public FontUtils(Context context, AppCompatDelegate appCompatDelegate) {
        if (sTypeface == null) {
            sTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/ocnyangfont.ttf");
        }
        this.appCompatDelegate = appCompatDelegate;
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View view = appCompatDelegate.createView(parent, name, context, attrs);
        if (view instanceof TextView) {
            ((TextView) view).setTypeface(sTypeface);
        }
        return view;
    }
}
