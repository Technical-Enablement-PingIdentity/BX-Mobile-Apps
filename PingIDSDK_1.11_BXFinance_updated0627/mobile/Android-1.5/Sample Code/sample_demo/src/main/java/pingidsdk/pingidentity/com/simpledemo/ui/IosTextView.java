package pingidsdk.pingidentity.com.simpledemo.ui;

import android.content.Context;
import android.graphics.Typeface;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;


/**
 * Created by Ping Identity on 11/6/18.
 */

public class IosTextView extends AppCompatTextView {
    public IosTextView(Context context) {
        super(context);
        init(null);
    }

    public IosTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public IosTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
           Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/HelveticaNeueLight.ttf");setTypeface(myTypeface);
        }
    }
}
