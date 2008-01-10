package hudson.plugins.sfee;

import hudson.plugins.sfee.webservice.LoginFault;
import hudson.plugins.sfee.webservice.SourceForgeSoap;
import hudson.plugins.sfee.webservice.SystemFault;

import java.lang.reflect.Method;
import java.net.URL;
import java.rmi.RemoteException;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.AuthenticationServiceException;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;

/**
 * @author huybrechts
 */
public class SFEEAuthenticationManager implements AuthenticationManager {

	private SourceForgeSoap sfSoap;
	private String host;
	
	public SFEEAuthenticationManager(String host) {
		this.host = host;
	}

	// TODO is this correct ?
	private static final GrantedAuthority[] TEST_AUTHORITY = {
			new GrantedAuthorityImpl("authenticated"), new GrantedAuthorityImpl("admin") };

	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		sfSoap = getSourceForgeApp(host, SourceForgeSoap.class);

		String userId = (String) authentication.getPrincipal();
		String password = (String) authentication.getCredentials();

		try {
			String sessionId = sfSoap.login(userId, password);
			
			// login succeeded

			return new SFEEAuthentication(userId, password, TEST_AUTHORITY,
					sessionId);
		} catch (LoginFault e) {
			throw new BadCredentialsException("Wrong username or password.");
		} catch (SystemFault e) {
			throw new AuthenticationServiceException("Error while contacting SFEE", e);
		} catch (RemoteException e) {
			throw new AuthenticationServiceException("Error while contacting SFEE", e);
		}
	}

	/**
	 * Returns a stub for the webservice.
	 */
	@SuppressWarnings("unchecked")
	private <T> T getSourceForgeApp(String host, Class<T> klazz) {
		try {
			URL endpoint = new URL("http://" + host
					+ ":8080/sf-soap43/services/"
					+ klazz.getSimpleName().replace("Soap", ""));
			String serviceName = klazz.getSimpleName();
			String packageName = klazz.getPackage().getName();
			serviceName = serviceName
					.substring(0, serviceName.length() - 4);
			String stubName = packageName + "." + serviceName
					+ "SoapServiceLocator";
			Class stubClass = Class.forName(stubName);
			Method m = stubClass.getMethod("get" + serviceName,
					new Class[] { URL.class });
			return (T) m.invoke(stubClass.newInstance(),
					new Object[] { endpoint });
		} catch (Exception e) {
			throw new RuntimeException("Error getting service stub", e);
		}
	}
}
