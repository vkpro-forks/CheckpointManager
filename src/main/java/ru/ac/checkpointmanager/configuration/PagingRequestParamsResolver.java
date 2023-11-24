package ru.ac.checkpointmanager.configuration;

import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.RequestParamMethodArgumentResolver;
import ru.ac.checkpointmanager.dto.passes.PagingParams;

public class PagingRequestParamsResolver extends RequestParamMethodArgumentResolver {

    private static final String PAGE = "page";

    public static final String SIZE = "size";


    public PagingRequestParamsResolver(boolean useDefaultResolution) {
        super(useDefaultResolution);
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(PagingParam.class);
    }

    @Override
    protected Object resolveName(@NonNull String name,
                                 @NonNull MethodParameter parameter,
                                 @NonNull NativeWebRequest request) {
        return new PagingParams(
                resolvePage(request),
                resolveSize(request)
        );
    }

    private Integer resolvePage(NativeWebRequest request) {
        String page = request.getParameter(PAGE);
        return page == null ? 0 : Integer.parseInt(page);
    }

    private Integer resolveSize(NativeWebRequest request) {
        String size = request.getParameter(SIZE);
        return size == null ? 10 : Integer.parseInt(size);
    }
}
