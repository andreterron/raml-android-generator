{%- import "../../javaBase.njk" as java -%}
{% call java.wrapImports(app, '.service.api.utils') -%}

import android.net.Uri;

import org.hamcrest.CustomTypeSafeMatcher;

import java.net.URI;
import java.util.List;

/**
 * Created by andreterron on 10/21/15.
 */
public class UrlPathMatcher extends CustomTypeSafeMatcher<String> {

    protected Uri mPath;
    protected List<String> mSegments;


    public UrlPathMatcher(String rootUrl, String path) {
        super(path);
        mPath = Uri.parse(rootUrl.concat(path));
        mSegments = mPath.getPathSegments();
    }

    public UrlPathMatcher(String path) {
        super(path);
        mPath = new Uri.Builder().path(path).build();
        mSegments = mPath.getPathSegments();
    }

    @Override
    protected boolean matchesSafely(String item) {
        // Extracts segments
        List<String> segments = Uri.parse(item).getPathSegments();

        // Compare number of segments
        if (segments.size() != mSegments.size()) {
            return false;
        }

        // Compare each segment
        String seg;
        for (int i = 0; i < mSegments.size(); i++) {
            seg = mSegments.get(i);
            if (!seg.equals(segments.get(i)) && (!seg.startsWith("{") || !seg.endsWith("}"))) {
                return false;
            }
        }
        return true;
    }
}

{%- endcall %}
