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

package com.example.app.finalproject.model;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.util.Iterator;
import java.util.List;

import com.i2rd.hibernate.AbstractProcessor;

import net.proteusframework.ui.search.JoinedQLBuilder;
import net.proteusframework.ui.search.PropertyConstraint;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.QLBuilderImpl;
import net.proteusframework.users.model.PasswordCredentials;

/**
 * Dao for facultyMember
 *
 * @author Fajie Han (fhan@venturetechasia.net)
 * @since 14-11-7 ??1:36
 */
@Repository(FacultyMemberDao.RESOURCE_NAME)
public class FacultyMemberDao extends AbstractProcessor<FacultyMemberProfile>
{
    public static final String RESOURCE_NAME = "com.example.app.finalproject.model.FacultyMemberDao";
    /** FacultyMemberProfile */
    private FacultyMemberProfile _facultyMemberProfile;

    /**
     * get all the facultyMemberProfile that is not deleted
     * and that current user
     * @return qb
     */
    public QLBuilder getAllFacultyQB()
    {
        final QLBuilder qb = new QLBuilderImpl(FacultyMemberProfile.class,"FacultyMemberProfile");
        qb.appendCriteria("deleted", PropertyConstraint.Operator.eq,"f");
        JoinedQLBuilder cred = qb.createJoin(QLBuilder.JoinType.LEFT, "createUser.credentials", "cred");
        cred.appendCriteria("class", PropertyConstraint.Operator.eq, PasswordCredentials.class.getName());
        return qb;
    }

    /**
     * Save an entity
     * @param facultyMemberProfile the entity to save
     * @return <code>true</code> if success, <code>false</code> otherwise.
     */
    public boolean saveFacultyMemberProfile(FacultyMemberProfile facultyMemberProfile)
    {
        Session session = getSession();
        beginTransaction();
        boolean success = false;
        try
        {
            session.saveOrUpdate(facultyMemberProfile);
            success = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(success)
                commitTransaction();
            else
                rollbackTransaction();
        }
        return success;
    }

    @Override
    public Class<FacultyMemberProfile> getEntityType()
    {
      return FacultyMemberProfile.class;
    }

    /**
     * To get the specified facultyMemberProfile when click one of the listing members
     * @param slug
     * @return the _facultyMemberProfile
     */
    public FacultyMemberProfile getFacultyMemberProfile(String slug)
    {
        QLBuilder proQB = getAllFacultyQB();
        List<FacultyMemberProfile> facultyMemberProfileList=proQB.getQueryResolver().list();
        Iterator<FacultyMemberProfile> profileIterator = facultyMemberProfileList.iterator();
        while(profileIterator.hasNext())
        {
            _facultyMemberProfile = profileIterator.next();
            if(_facultyMemberProfile.getSlug().equals(slug))
            {
                return _facultyMemberProfile;
            }
        }
        return _facultyMemberProfile;
    }

    /**
     * To create a new FacultyMemberProfile if it doesn't provide one
     *
     * @return FacultyMemberProfile
     */
    public FacultyMemberProfile getAttachedFacultyMemberProfile(FacultyMemberProfile facultyMemberProfile)
    {
        if (facultyMemberProfile==null)
        {
            facultyMemberProfile = new FacultyMemberProfile();
            return facultyMemberProfile;
        }
        return facultyMemberProfile;
    }

    /**
     * To judge whether a name already exists when add a new member
     * @param slug
     * @return <code>true</code> if exist.
     */
    public boolean getFlag(String slug)
    {
        QLBuilder proQB = getAllFacultyQB();
        List<FacultyMemberProfile> facultyMemberProfileList=proQB.getQueryResolver().list();
        Iterator<FacultyMemberProfile> profileIterator = facultyMemberProfileList.iterator();
        while(profileIterator.hasNext())
        {
            _facultyMemberProfile = profileIterator.next();
            if(_facultyMemberProfile.getSlug().equals(slug))
            {
                return true;
            }
        }
        return true;
    }
}
