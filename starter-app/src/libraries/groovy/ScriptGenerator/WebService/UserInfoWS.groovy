/*
 * Copyright (c) Interactive Information R & D (I2RD) LLC.
 * All Rights Reserved.
 *
 * This software is confidential and proprietary information of
 * I2RD LLC ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered
 * into with I2RD.
 */

package ScriptGenerator.WebService

/**
 * WebService that provides current user information.
 * @author Russ Tennant (russ@venturetech.net)
 */
/*
class UserInfo extends AbstractScriptGenerator
{
    static final PRINCIPAL_ID = 'principal_id'
    static final TOURED = 'toured'

    static Set<String> methods;
    static{
        Set<String> myMethods = new HashSet<>();
        myMethods.add('GET');
        myMethods.add('POST');
        methods = Collections.unmodifiableSet(myMethods)
    }


    @Autowired
    PlanDAO _planDAO;
    @Autowired
    UserDAO _userDAO;
    @Autowired
    CompanyDAO _companyDAO;

    @Override
    Set<String> getAllowedMethods(CmsRequest request) { return methods }

    @Override
    Scope getScope() { Scope.REQUEST }

    @Override
    ContentType negotiateContentType(CmsRequest request, List accept, UserAgent ua)
    {
        return ContentTypes.Application.json.contentType
    }

    @Override
    void preRenderProcess(CmsRequest request, CmsResponse response, ProcessChain chain)
    {
        def principal = request.getPrincipal()
        if(principal == null)
        {
            response.sendError(RequestError.UNAUTHORIZED, 'Not authorized')
            return
        }
        if(!request.getMethod().equalsIgnoreCase('post'))
            return

        Preferences touredPrefNode = getTouredPreferencesNode()

        Gson gson = new GsonBuilder()
            .create()
        def json = gson.fromJson(request.getReader(), JsonObject.class)
        def pid = json.getAsJsonPrimitive(PRINCIPAL_ID)?.getAsLong()
        if(!principal.getId().equals(pid))
        {
            response.sendError(RequestError.FORBIDDEN, 'Forbidden')
            return
        }
        JsonObject toured = json.getAsJsonObject(TOURED)
        def touredEntries = toured.entrySet()
        if(touredEntries.size() > 10)
        {
            response.sendError(RequestError.BAD_REQUEST, 'Excessive size')
            return
        }
        touredEntries.each {Map.Entry<String, JsonElement> node ->
            touredPrefNode.putBoolean(node.key, node.value.getAsBoolean())
        }

    }

    def Preferences getTouredPreferencesNode()
    {
        def touredPrefNode = Preferences.userRoot().node('/app/toured')
        return touredPrefNode
    }

    @Override
    void render(CmsRequest request, CmsResponse response, RenderChain chain) throws IOException
    {
        if(!request.getMethod().equalsIgnoreCase('get'))
            return
        def user = _userDAO.getAssertedCurrentUser()
        def membershipTypes = _planDAO.getPlanMembershipTypesForUserAndCoaching(user,
            _companyDAO.getAssertedCoachingEntityForUser(user), true)
        JsonObject toured = new JsonObject()
        JsonArray roles = new JsonArray()
        membershipTypes.each {
            roles.add(new JsonPrimitive(it.programmaticIdentifier))
            toured.addProperty(it.programmaticIdentifier, false)
        }

        Gson gson = new GsonBuilder()
            .create()
        JsonObject responseObject = new JsonObject()
        responseObject.addProperty(PRINCIPAL_ID, request.getPrincipal().getId())
        responseObject.add('roles', roles)
        responseObject.add(TOURED, toured)

        def touredPreferencesNode = getTouredPreferencesNode()
        touredPreferencesNode.keys().each {
            toured.addProperty(it, touredPreferencesNode.getBoolean(it, false))
        }

        gson.toJson(responseObject, response.getContentWriter())
    }
}


generator = new UserInfo()
def applicationContext = ApplicationContextUtils.instance.context
applicationContext.autowireCapableBeanFactory.autowireBean(generator)
return generator
*/