
package services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import utilities.AbstractTest;
import domain.SystemConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
	"classpath:spring/junit.xml"
})
@Transactional
public class SystemConfigurationServiceTest extends AbstractTest {

	// The SUT -------------------------------------------------------------
	@Autowired
	private SystemConfigurationService	sysConService;

	@Autowired
	private AdministratorService		adminService;


	// Tests ---------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void driverModifyingCache() {

		final Collection<String> banners = new ArrayList<String>();
		final String url = "http://www.bouncepen.com/wp-content/themes/twentyfifteen/uploads/user-photo/dummy-image.png";
		banners.add(url);
		final Collection<String> bannersEmpty = new ArrayList<String>();
		final Collection<String> bannersFull = new ArrayList<String>();
		final Collection<String> bannersWrong = new ArrayList<String>();
		for (int i = 0; i < 20; i++)
			bannersFull.add(url);
		final String urlWrong = "Esto no es un link";
		bannersWrong.add(urlWrong);

		final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		try {
			final Date dateWrong = sdf.parse("10:00:00");
			final Date dateRight = sdf.parse("13:00:00");

			final Object testingData[][] = {
				{	// Modificaci�n correcta: Cach� correcta.
					"admin", bannersFull, dateRight, null
				}, { // Modificacion erronea: Cache err�nea.
					"admin", bannersEmpty, dateWrong, IllegalArgumentException.class
				}, { // Modificacion erronea: Banners vac�os.
					"admin", bannersEmpty, dateRight, IllegalArgumentException.class
				}, { // Modificacion erronea: Banners con formato erroneo.
					"admin", bannersWrong, dateRight, IllegalArgumentException.class
				}, { // Modificacion erronea: Banners completo.
					"admin", bannersFull, dateRight, IllegalArgumentException.class
				}
			};
			for (int i = 0; i < testingData.length; i++)
				this.templateModifyingCache((String) testingData[i][0], (Collection<String>) testingData[i][1], (Date) testingData[i][2], (Class<?>) testingData[i][3]);
		} catch (final ParseException e) {
			e.printStackTrace();
		}
	}
	// Templates ----------------------------------------------------------
	protected void templateModifyingCache(final String username, final Collection<String> banners, final Date cacheTime, final Class<?> expected) {
		Class<?> caught;
		caught = null;
		try {
			this.adminService.findByPrincipal();
			this.authenticate(username);
			final SystemConfiguration sc = this.sysConService.findMain();
			sc.setBanners(banners);
			sc.setCacheTime(cacheTime);
			this.sysConService.save(sc);
			this.sysConService.flush();
			this.unauthenticate();
		} catch (final Throwable oops) {
			caught = oops.getClass();
		}
		this.checkExceptions(expected, caught);
	}

}
