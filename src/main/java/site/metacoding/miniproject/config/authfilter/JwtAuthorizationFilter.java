package site.metacoding.miniproject.config.authfilter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import lombok.extern.slf4j.Slf4j;
import site.metacoding.miniproject.dto.user.UserRespDto.SignedDto;
import site.metacoding.miniproject.exception.ApiException;
import site.metacoding.miniproject.utill.JWTToken.CookieForToken;
import site.metacoding.miniproject.utill.JWTToken.HeaderForToken;
import site.metacoding.miniproject.utill.JWTToken.TokenToSinedDto;
import site.metacoding.miniproject.utill.SecretKey;
@Slf4j
public class JwtAuthorizationFilter implements Filter {

    DecodedJWT decodedJWT;
    String tokenForHeader = null;
    String tokenForCookie = null;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        tokenForCookie = CookieForToken.cookieToToken(req.getCookies());


        if (req.getHeader("Authorization") != null) {
            tokenForHeader = HeaderForToken.HeaderToToken(req.getHeader("Authorization"));
        }
        

        if (tokenForCookie != null) {
            decodedJWT = JWT.require(Algorithm.HMAC512(SecretKey.SECRETKEY.key())).build()
                    .verify(tokenForCookie);
        }

        if (tokenForHeader != null) {
            decodedJWT = JWT.require(Algorithm.HMAC512(SecretKey.SECRETKEY.key())).build()
                    .verify(tokenForHeader);
        }

        log.info("debug: cookie" + tokenForCookie);
        log.info("debug :  header" + tokenForHeader);

        if (decodedJWT == null) {
            throw new ApiException("인증 필요");
        }
        


        // map 형식으로 저장되어있는 토큰값을 map형식으로 가져온다.
        Map<String, Object> getSigned = decodedJWT.getClaim("sigendDto").asMap();

        TokenToSinedDto tokenToSinedDto = new TokenToSinedDto();
        SignedDto<?> signedDto = tokenToSinedDto.tokenToSignedDto(getSigned);

        HttpSession session = req.getSession();

        session.setAttribute("principal", signedDto);

        chain.doFilter(req, resp);
    }

}
