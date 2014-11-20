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

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

import com.i2rd.hibernate.AbstractProcessor;

import net.proteusframework.core.spring.ApplicationContextUtils;
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
    /** Logger */
    private final static Logger _logger = Logger.getLogger(FacultyMemberDao.class);

    /**
     * @return the RESOURCE_NAME
     */
    public static FacultyMemberDao getInstance()
    {
        return (FacultyMemberDao) ApplicationContextUtils.getInstance().getContext().getBean(RESOURCE_NAME);
    }

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
        catch (NullPointerException e)
        {
            _logger.error("Save FacultyMemberProfile error.", e);
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

    public Long getId(FacultyMemberProfile facultyMemberProfile,Long id,String slug)
    {
        QLBuilder proQB = getAllFacultyQB();
        List<FacultyMemberProfile> facultyMemberProfileList=proQB.getQueryResolver().list();
        for (int index=0;index<facultyMemberProfileList.size();index++)
        {
            if(facultyMemberProfileList.get(index).getSlug().equals(slug))
            {
                id = facultyMemberProfileList.get(index).getId();
            }

            else
            {
                continue;
            }

            break;
        }
        return id;
    }

}
