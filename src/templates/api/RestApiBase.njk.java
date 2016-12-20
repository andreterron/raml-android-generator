{%- import "./../javaBase.njk" as java -%}

{% call java.wrapImports(app, '.service.api') %}

import android.support.annotation.Nullable;

import bolts.Continuation;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import bolts.Task;
{%- call java.importClass('.service.api.security.ApiAuthentication') %}{% endcall %}
{%- call java.importClass('.service.api.security.ApiNoAuthentication') %}{% endcall %}
{%- call java.importClass('.service.api.utils.PipeContinuation') %}{% endcall %}

/**
 * Created by andreterron on 9/29/15.
 */
@Singleton
public class RestApiBase {

    protected String mRootUrl;
    protected RestOperations mRestTemplate;
    protected ApiAuthentication mDefaultAuthentication = new ApiNoAuthentication();

    @Inject
    public RestApiBase(@Named("rootUrl") String rootUrl, RestOperations restTemplate) {
        mRootUrl = rootUrl;
        mRestTemplate = restTemplate;
    }

    public void setDefaultAuthentication(ApiAuthentication defaultAuthentication) {
        mDefaultAuthentication = defaultAuthentication;
    }

    public UriComponentsBuilder uriBuilder(String path) {
        return UriComponentsBuilder.fromHttpUrl(mRootUrl.concat(path));
    }



    public class RequestBuilder<REQ, RES> {
        protected UriComponentsBuilder mUriBuilder;
        protected HttpMethod mMethod;
        protected REQ mParam;
        protected Class<RES> mResponseType;
        protected HttpHeaders mHeaders;
        protected ApiAuthentication mAuthentication;

        public RequestBuilder(UriComponentsBuilder uriBuilder, HttpMethod method, REQ param, Class<RES> responseType) {
            mUriBuilder = uriBuilder;
            mMethod = method;
            mParam = param;
            mResponseType = responseType;
            mAuthentication = mDefaultAuthentication;
        }

        public RequestBuilder<REQ, RES> headers(MultiValueMap<String, String> headers) {
            getHeaders().putAll(headers);
            return this;
        }

        public HttpHeaders getHeaders() {
            if (mHeaders == null) {
                mHeaders = new HttpHeaders();
            }
            return mHeaders;
        }

        public RequestBuilder<REQ, RES> authentication(ApiAuthentication auth) {
            if (auth != null) {
                mAuthentication = auth;
            }
            return this;
        }

        public RequestBuilder<REQ, RES> mediaType(MediaType mediaType) {
            getHeaders().setContentType(mediaType);
            return this;
        }

        public Task<RES> execute() {
            return makeUrlRequest(mUriBuilder, mMethod, mParam, mHeaders, mAuthentication, mResponseType, new HashMap<String, Object>());
        }

        public Task<RES> execute(Map<String, ?> urlParams) {
            return makeUrlRequest(mUriBuilder, mMethod, mParam, mHeaders, mAuthentication, mResponseType, urlParams);
        }
    }

    public <REQ, RES> Task<RES> makeRequest(
            String path,
            HttpMethod method,
            REQ param,
            Class<RES> responseType,
            Map<String, ?> urlParams) {
        return makeUrlRequest(uriBuilder(path), method, param, null, mDefaultAuthentication, responseType, urlParams);
    }

    public <REQ, RES> Task<RES> makeRequest(
            UriComponentsBuilder uriBuilder,
            HttpMethod method,
            REQ param,
            Class<RES> responseType,
            Map<String, ?> urlParams) {
        return makeUrlRequest(uriBuilder, method, param, null, mDefaultAuthentication, responseType, urlParams);
    }


    public <REQ, RES> RequestBuilder<REQ, RES> requestBuilder(
            String path,
            HttpMethod method,
            REQ param,
            Class<RES> responseType) {
        return new RequestBuilder<>(uriBuilder(path), method, param, responseType);
    }


    public <REQ, RES> RequestBuilder<REQ, RES> requestBuilder(
            UriComponentsBuilder uriBuilder,
            HttpMethod method,
            REQ param,
            Class<RES> responseType) {
        return new RequestBuilder<>(uriBuilder, method, param, responseType);
    }

    private <REQ, RES> Task<RES> makeUrlRequest(
            final UriComponentsBuilder uriBuilder,
            final HttpMethod method,
            REQ param,
            MultiValueMap<String, String> headers,
            ApiAuthentication auth,
            final Class<RES> responseType,
            @Nullable final Map<String, ?> urlParams) {
        if (auth != null) {
            headers = auth.apply(uriBuilder, headers);
        }
        final String url = uriBuilder.build().toUriString();
        final HttpEntity<REQ> request = new HttpEntity<>(param, headers);

        final Callable<RES> requestCallable = new Callable<RES>() {
            @Override
            public RES call() throws Exception {
                ResponseEntity<RES> response = mRestTemplate.exchange(
                        url,
                        method,
                        request,
                        responseType,
                        urlParams);
                if (response.getStatusCode().series() != HttpStatus.Series.SUCCESSFUL) {
                    throw new HttpServerErrorException(response.getStatusCode(), response.getStatusCode().getReasonPhrase());
                }
                return response.getBody();
            }
        };

        Task<RES> task = Task.callInBackground(requestCallable);
        for (int retry = 3; retry > 0; retry--) {
            task = task.continueWithTask(new Continuation<RES, Task<RES>>() {
                @Override
                public Task<RES> then(Task<RES> task) throws Exception {
                    if (task.isFaulted()) {
                        return Task.delay(300).continueWithTask(new Continuation<Void, Task<RES>>() {
                            @Override
                            public Task<RES> then(Task<Void> task) throws Exception {
                                return Task.callInBackground(requestCallable);
                            }
                        });
                    } else {
                        return task;
                    }
                }
            });
        }

        return task.continueWithTask(new PipeContinuation<RES>(), Task.UI_THREAD_EXECUTOR);
    }
}

{%- endcall %}
