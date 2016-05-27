import net.proteusframework.core.spring.ApplicationContextUtils
import net.proteusframework.users.model.dao.PrincipalDAO
import org.springframework.beans.factory.annotation.Autowired

class AutowireTest
{
    @Autowired
    PrincipalDAO dao
}


def scriptable = new AutowireTest()
def applicationContext = ApplicationContextUtils.instance.context
applicationContext.autowireCapableBeanFactory.autowireBean(scriptable)
return scriptable
