package net.somewhere.gpxclone.services;

import net.somewhere.gpxclone.utils.UserAlreadyExistsException;
import java.util.*;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.MessageSource;

import net.somewhere.gpxclone.entities.*;
import net.somewhere.gpxclone.dao.*;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.thymeleaf.context.Context;
import org.thymeleaf.spring3.SpringTemplateEngine;

@Transactional
public class UserServiceImpl implements UserService,UserDetailsService {

    private UserDao userDao;
    private RoleDao roleDao;
    private PasswordEncoder springSecurityEncoder;
    private String activationLink;
    private JavaMailSender mailSender;
    private SpringTemplateEngine templateEngine;
    
    private MessageSource mailMessages;

    public void setMailMessages(MessageSource mailMessages)
    {this.mailMessages = mailMessages;}
        
    public void setTemplateEngine(SpringTemplateEngine templateEngine)
    {this.templateEngine = templateEngine;}

    public void setActivationLink(String activationLink)
    {this.activationLink = activationLink;}

    public void setUserDao(UserDao userDao)
    {this.userDao = userDao;}

    public void setRoleDao(RoleDao roleDao)
    {this.roleDao = roleDao;}

    public void setMailSender(JavaMailSender mailSender) 
    {this.mailSender = mailSender;}
    
    public void setSpringSecurityEncoder(PasswordEncoder encoder)
    {this.springSecurityEncoder = encoder;}

    public String activateUser(String activationKey)
    {return userDao.activateUser(activationKey);}

    public void resetPassword(String userId, String email) throws Exception
    {
        if (userId == null || email == null) return;
        User user = userDao.getUser(userId);
        if (user != null) {
            String userEmailOnRecord = user.getEmail();
            if (userEmailOnRecord != null && userEmailOnRecord.length() > 0) {
                if (userEmailOnRecord.equalsIgnoreCase(email.trim())) {
                    String tempPasswd = tempPassword();
                    user.setPassword(springSecurityEncoder.encode(tempPasswd));
                    //user.setTemporaryPassword(true);
                    userDao.updateUser(user);
                    HashMap<String,Object> ThymeMailTemplateMap = new HashMap();
                    ThymeMailTemplateMap.put("username",user.getUsername());
                    ThymeMailTemplateMap.put("tempPasswd",tempPasswd);
                    sendThymeMail(userEmailOnRecord, "password_reset_email", ThymeMailTemplateMap, Locale.getDefault());
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public boolean hasUser(String userId) {
        return userDao.userExists(userId);
    }

    @Transactional(readOnly = true)
    public User getUser(String userId) {
        return userDao.getUser(userId);
    }

    public User createUser(User userDto) throws UserAlreadyExistsException{
        if(userDao.userExists(userDto.getUsername()))
        {
            throw new UserAlreadyExistsException(userDto.getUsername());
        } 
        
        if (userDto.IsApproved())
        {
            userDto.setActivationKey(null);
        }
        else
        {
            //userDto.setActivationKey(PasswordSupport.getMD5Hash(userDto.getLastName()+userDto.getUsername()+userDto.getEmail()+userDto.getFirstName()));
            userDto.setActivationKey(springSecurityEncoder.encode(userDto.getPId()+userDto.getUsername()+userDto.getEmail()));
        }
        userDto.setPassword(springSecurityEncoder.encode(userDto.getPassword()));
        User user = userDao.createUser(userDto);
        
        if (!userDto.IsApproved()) {
            String email = user.getEmail();
            String activationKey = user.getActivationKey();
            if (activationLink != null && email != null && activationKey != null) {
                try {
                    String href = activationLink+((activationLink.indexOf("?") != -1)?"&key=":"?key=")+activationKey;                   
                    HashMap<String,Object> ThymeMailTemplateMap = new HashMap();
                    ThymeMailTemplateMap.put("username",user.getUsername());
                    ThymeMailTemplateMap.put("activationLink",href);
                    ThymeMailTemplateMap.put("subscriptionDate", new Date());                   
                    sendThymeMail(email, "account_activation_email", ThymeMailTemplateMap, Locale.getDefault());
                } catch (Exception zzz) {
                    String tmp=zzz.getMessage();
                    tmp.concat("");
                }
            }
        }
        Role userRole = roleDao.getRole("user");
        if (userRole == null) {
            userRole = roleDao.createRole("user", "General user role.");
        }
        //user.addRole(userRole);
        user.addRole(userRole);
        return user;
    }

    @Transactional(readOnly = true)
    public boolean isUserInRole(String userName, String roleName) {
        if (userName == null || roleName == null) return false;
        User user = userDao.getUser(userName);
        if (user == null) return false;
        return user.hasRole(roleName);
    }

    public void updateUser(User userDto) {
        userDao.updateUser(userDto);
    }

    public void updateUserPreferences(UserPreferences prefs) {
        userDao.updateUserPreferences(prefs);
    }

    public boolean deleteUser(User user) {
        return userDao.deleteUser(user);
    }

    @Transactional(readOnly = true)
    public Collection<User> getUsers() {
        return userDao.getUsers();
    }

    public Role createRole(String roleName, String description) {
        return roleDao.createRole(roleName, description);
    }

    public void updateRole(Role role) {
        roleDao.updateRole(role);
    }

    public boolean deleteRole(Role role) {
        return roleDao.deleteRole(role);
    }

    @Transactional(readOnly = true)
    public Role getRole(String roleName) {
        return roleDao.getRole(roleName);
    }

    @Transactional(readOnly = true)
    public Collection<Role> getRoles() {
        return roleDao.getRoles();
    }
    
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException
    {
        //org.springframework.security.core.userdetails.User springSecurityUser;
        User userEntity=userDao.getUser(userName);
        
        if (userEntity==null)
        {
            throw new UsernameNotFoundException(userName);
        }
        /*
        String username = userEntity.getUsername();
        String password = userEntity.getPassword();
        boolean enabled = userEntity.IsApproved();
        boolean accountNonExpired = userEntity.IsApproved();
        boolean credentialsNonExpired = userEntity.IsApproved();
        boolean accountNonLocked = ! userEntity.IsLockedOut();
        
        Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        for (Role role : userEntity.getRoles())
        {
          authorities.add(new SimpleGrantedAuthority(role.getRolename()));
        }

        springSecurityUser = new  org.springframework.security.core.userdetails.User
                (userEntity.getUsername(), userEntity.getPassword(),
                enabled,accountNonExpired, credentialsNonExpired,accountNonLocked,
                authorities);


        return springSecurityUser;*/
        return userEntity;
    }

   
   
    protected void sendThymeMail(String emailAddress, String l7dKey, Map<String,?> l7dArgs, Locale locale) throws MessagingException
    {
        //ResourceBundle rb = ResourceBundle.getBundle("example.i18n", locale);
        //String body = MessageFormat.format(rb.getString(l7dKey), l7dArgs);
        //String subject = rb.getString(l7dKey+"_subject");
        String subject = mailMessages.getMessage(l7dKey+"_subject", null, locale);
        // Prepare the evaluation context
        //final Context ctx = new Context(locale);
        final Context ctx = new Context(locale,l7dArgs);
        //ctx.setVariable("name", recipientName);
        //ctx.setVariable("username", "someone");
        //ctx.setVariable("activationLink","http://wwww.somewhere.net");
        
        //ctx.setVariable("name", emailAddress);
        //ctx.setVariable("subscriptionDate", new Date());
        //ctx.setVariable("hobbies", Arrays.asList("Cinema", "Sports", "Music"));
        //ctx.setVariable("imageResourceName", imageResourceName); // so that we can reference it from HTML

        // Prepare message using a Spring helper
        final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper message = 
                new MimeMessageHelper(mimeMessage, true /* multipart */, "UTF-8");
        message.setSubject(subject);
        message.setFrom("thymeleaf@example.com");
        message.setTo(emailAddress);

        // Create the HTML body using Thymeleaf
        //final String htmlContent = this.templateEngine.process(l7dKey, ctx);
        final String htmlContent = this.templateEngine.process(l7dKey, ctx);
        message.setText(htmlContent, true /* isHtml */);

        // Add the inline image, referenced from the HTML code as "cid:${imageResourceName}"
        //final InputStreamSource imageSource = new ByteArrayResource(imageBytes);
        //message.addInline(imageResourceName, imageSource, imageContentType);

        // Send mail
        this.mailSender.send(mimeMessage); 
    }
    
    private static final String tempPassword()
    {
        java.util.Random rand = new java.util.Random();
        int[] aNums = new int[8];
        for (int n=0; n < aNums.length; n++) aNums[n] = rand.nextInt(9)+1;
        char[] ach1 = new char[]{'a','b','c','d','e','f','g','h','i','j'};
        char[] ach2 = new char[]{'K','L','M','N','P','Q','R','S','T','U'};
        char[] ach3 = new char[]{'v','w','x','y','z','V','W','X','Y','Z'};
        char[] ach4 = new char[]{'k','$','m','n','p','q','r','s','t','u'};
        char[] ach5 = new char[]{'$','%','!','#','$','%','!','#','$','%'};
        return (ach4[aNums[7]]+String.valueOf(aNums[2])+ach1[aNums[3]]+String.valueOf(aNums[0])+ach3[aNums[5]]+ach2[aNums[4]]+ach4[aNums[6]]+ach5[aNums[1]]);
    }

    private Exception DataIntegrityViolationException(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
