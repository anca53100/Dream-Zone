package com.example.dreamzone.interceptor;

import com.example.dreamzone.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class SessionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest req,
                             HttpServletResponse res,
                             Object handler) throws Exception {
        String uri = req.getRequestURI();

        if (uri.startsWith("/auth") || uri.startsWith("/css")
                || uri.startsWith("/js") || uri.startsWith("/images")
                || uri.startsWith("/favicon")) {
            return true;
        }

        Usuario usuario = (Usuario) req.getSession().getAttribute("usuarioLogueado");

        if (usuario == null) {
            res.sendRedirect("/auth/login");
            return false;
        }

        if (uri.startsWith("/admin") && !"ROLE_ADMIN".equals(usuario.getRol())) {
            res.sendRedirect("/");
            return false;
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest req,
                           HttpServletResponse res,
                           Object handler,
                           ModelAndView mav) {
        if (mav != null) {
            Usuario usuario = (Usuario) req.getSession().getAttribute("usuarioLogueado");
            if (usuario != null) {
                mav.addObject("usuarioLogueado", usuario);
                mav.addObject("esAdmin", "ROLE_ADMIN".equals(usuario.getRol()));
            }
        }
    }
}