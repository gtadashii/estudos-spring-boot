package br.com.gtadashii.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.gtadashii.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter{

  @Autowired
  private IUserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

        var servletPath = request.getServletPath();

        if (servletPath.startsWith("/tasks/")) {
          var authorization = request.getHeader("Authorization");

          var encodedAuth = authorization.substring("Basic".length()).trim();

          byte[] decodedAuth = Base64.getDecoder().decode(encodedAuth);

          var authString = new String(decodedAuth);

          String[] credentials = authString.split(":");
          String userName = credentials[0];
          String password = credentials[1];

          var user = this.userRepository.findByUsername(userName);

          if (user == null) {
            response.sendError(401);
          } else {
            var passwordVerification = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
            if (passwordVerification.verified) {
              request.setAttribute("userId", user.getId());
              filterChain.doFilter(request, response);
            } else {
              response.sendError(401);
            }
          }
        } else {
          filterChain.doFilter(request, response);
        }
  }
}
