{%- import "../../javaBase.njk" as java -%}
{% call java.wrapImports(app, '.service.api.utils') -%}

import bolts.Continuation;
import bolts.Task;

/**
 * Created by andreterron on 11/5/15.
 */
public class PipeContinuation<RES> implements Continuation<RES, Task<RES>> {
    @Override
    public Task<RES> then(Task<RES> task) throws Exception {
        return task;
    }
}

{%- endcall %}
