package com.v5analytics.webster.resultWriters;

import com.v5analytics.webster.HandlerChain;
import com.v5analytics.webster.annotations.ContentType;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;

public class ResultWriterBase implements ResultWriter {
    private final String contentType;
    private final boolean voidReturn;

    public ResultWriterBase(Method handleMethod) {
        contentType = getContentType(handleMethod);
        voidReturn = handleMethod.getReturnType().equals(Void.TYPE) || handleMethod.getReturnType().equals(Void.class);
    }

    protected String getContentType(Method handleMethod) {
        ContentType contentTypeAnnotation = handleMethod.getAnnotation(ContentType.class);
        if (contentTypeAnnotation != null) {
            return contentTypeAnnotation.value();
        } else {
            return null;
        }
    }

    @Override
    public void write(Object result, HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        if (contentType != null) {
            response.setContentType(contentType);
        }
        if (voidReturn) {
            return;
        }
        if (result == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        ServletOutputStream out = response.getOutputStream();
        writeResult(out, result);
        out.close();
    }

    protected void writeResult(OutputStream out, Object result) throws IOException {
        out.write(getResultBytes(result));
    }

    protected byte[] getResultBytes(Object result) {
        if (result.getClass() == byte[].class) {
            return (byte[]) result;
        }
        return result.toString().getBytes();
    }
}