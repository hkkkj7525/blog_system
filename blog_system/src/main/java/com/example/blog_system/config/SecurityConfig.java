package com.example.blog_system.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final DataSource dataSource;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Value("${COOKIE.VALIDITY}")
    private Integer COOKIE_VALIDITY;

    // 使用构造器注入所有依赖
    @Autowired
    public SecurityConfig(DataSource dataSource,
                          UserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.dataSource = dataSource;
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    /**
     * 配置PasswordEncoder Bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 1、自定义用户访问控制
        http.authorizeRequests()
                .antMatchers("/", "/page/**", "/article/**", "/login", "/register", "/client/**").permitAll()
                .antMatchers("/back/**", "/assets/**", "/user/**", "/article_img/**").permitAll()
                .antMatchers("/api/redis/**", "/api/public/**", "/redis-test", "/rdm-test").permitAll()
                //  添加这行：允许认证相关接口无需认证
                .antMatchers("/api/auth/**").permitAll()  // 允许所有认证接口
                .antMatchers("/admin/**").hasRole("admin")
                .anyRequest().authenticated();



        // 添加JWT过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 2、自定义用户登录控制
        http.formLogin()
                .loginPage("/login")
                .usernameParameter("username").passwordParameter("password")
                .successHandler(new AuthenticationSuccessHandler() {
                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
                        String url = httpServletRequest.getParameter("url");
                        RequestCache requestCache = new HttpSessionRequestCache();
                        SavedRequest savedRequest = requestCache.getRequest(httpServletRequest, httpServletResponse);
                        if (savedRequest != null) {
                            httpServletResponse.sendRedirect(savedRequest.getRedirectUrl());
                        } else if (url != null && !url.equals("")) {
                            URL fullURL = new URL(url);
                            httpServletResponse.sendRedirect(fullURL.getPath());
                        } else {
                            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                            boolean isAdmin = authorities.contains(new SimpleGrantedAuthority("ROLE_admin"));
                            if (isAdmin) {
                                httpServletResponse.sendRedirect("/admin");
                            } else {
                                httpServletResponse.sendRedirect("/");
                            }
                        }
                    }
                })
                .failureHandler(new AuthenticationFailureHandler() {
                    @Override
                    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
                        String url = httpServletRequest.getParameter("url");
                        httpServletResponse.sendRedirect("/login?error&url=" + url);
                    }
                });

        // 3、设置用户登录后cookie有效期
        http.rememberMe().alwaysRemember(true).tokenValiditySeconds(COOKIE_VALIDITY);

        // 4、自定义用户退出控制
        http.logout().logoutUrl("/logout").logoutSuccessUrl("/");

        // 5、异常处理
        http.exceptionHandling()
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
                        RequestDispatcher dispatcher = httpServletRequest.getRequestDispatcher("/errorPage/comm/error_403");
                        dispatcher.forward(httpServletRequest, httpServletResponse);
                    }
                })
                .authenticationEntryPoint(jwtAuthenticationEntryPoint);

        // 6、禁用CSRF
        http.csrf().disable();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 使用自定义的UserDetailsService和密码编码器
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    /**
     * 配置AuthenticationManager Bean
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    
}