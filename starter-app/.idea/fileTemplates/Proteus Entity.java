#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME};#end
/**
 * FIXME - ${USER}, please document me.
 * @author ${Your_Full_Name} (${USER}@venturetech.net)
 */
@javax.persistence.Entity
public class ${Class_Name} extends AbstractAuditableEntity<java.lang.Long> {
  /** The database id column for this entity */
  public static final String ID_COLUMN = "${Class_Name.toLowerCase()}_id";
  private static final String GENERATOR = ProjectConfig.PROJECT_SCHEMA + '.' + ID_COLUMN + "_seq";
    
  /**
  * Create a new instance.
  */
  public ${Class_Name}() 
  {
    super();
  }
  
    @javax.persistence.Id
    @javax.persistence.Column(name = ID_COLUMN)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GENERATOR)
    @SequenceGenerator(name = GENERATOR, sequenceName = GENERATOR)
    @Override
    public java.lang.Long getId()
    {
        return super.getId();
    }
}
