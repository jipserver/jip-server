package jip.server

import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder

class SecurityFilters {
    def authenticationManager
    def filters = {
        all(uri:'/api/**') {
            before = {
                def authString = request.getHeader('Authorization')
                if(!authString){
                    response.sendError(401)
                    return false
                }
                def encodedPair = authString - 'Basic '
                def decodedPair =  new String(new sun.misc.BASE64Decoder().decodeBuffer(encodedPair));
                def credentials = decodedPair.split(':')

                if(User.findByUsername(credentials[0])){
                    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(credentials[0], credentials[1]);
                    try{
                        Authentication auth = authenticationManager.authenticate(token)
                        SecurityContextHolder.getContext().setAuthentication(auth)
                        return true
                    }catch (BadCredentialsException e){
                        response.sendError(401)
                        return false

                    }
                }else{
                    // try to identify a job
                    Job job = Job.findById(Long.parseLong(credentials[0]))
                    if(!job || job.token != credentials[1]){
                        response.sendError(401)
                        return false
                    }
                    User user = User.findById(job.ownerId)
                    if(!user){
                        response.sendError(401)
                        return false
                    }

                    Authentication authentication = new UsernamePasswordAuthenticationToken(user, null,
                            AuthorityUtils.createAuthorityList(user.authorities.collect {it.authority}.toArray(new String[0])));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    return true
                }
            }
        }
    }
}
