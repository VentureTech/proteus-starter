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

package com.example.app.yhuang;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jetbrains.annotations.Contract;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import net.proteusframework.core.hibernate.dao.DAOHelper;
import net.proteusframework.core.lang.CloseableIterator;
import net.proteusframework.core.spring.ApplicationContextUtils;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.QLResolver;
import net.proteusframework.ui.search.QLResolverOptions;

/***
 * Dao for professor profile
 *@author Yinge Huang (yhuang@venturetechasia.net)
 * @since 11/11/14 5:16 AM
 */
@Repository(ProfessorDAO.RESOURCE_NAME)
public class ProfessorDAO extends DAOHelper
{
	/** Resource */
	public static final String RESOURCE_NAME = "com.example.app.yhuang.model.ProfessorDAO";
	/** Logger. */
    private final static Logger _logger = Logger.getLogger(ProfessorDAO.class);

	/**
	 * get ProfessorProfileDAO intance
	 * @return ProfessorProfileDAO instance
	 */
	public static ProfessorDAO getInstance()
	{
		return (ProfessorDAO) ApplicationContextUtils.getInstance().getContext().getBean(RESOURCE_NAME);
	}

	/**
	 * get one professor by professor id
	 * @param id the professor id
	 * @return one professor object
	 */
	public Professor findById(Long id)
	{
		Session session = getSession();
		Professor professor=(Professor)session.load(Professor.class,id);
		return professor;
	}

	/**
	 * add one professor
	 * @param professor professor object
	 * @return true or false,if add successfully,return true
	 */
    public boolean saveProfessor(Professor professor)
    {
		Session session = getSession();
		beginTransaction();
		boolean success = false;
		try
		{
			if (isTransient(professor) || isAttached(professor))
			    session.saveOrUpdate(professor);
			else
				session.merge(professor);
			success = true;
		}
		catch (NullPointerException e)
		{
			_logger.error("Save Professor error.", e);
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

	/**
	 * delete one professor from db
	 * @param professor one professor object
	 */
    public void deleteProfessor(Professor professor)
    {
        beginTransaction();
        boolean success = false;
        try
        {
            final Session session = getSession();
            session.delete(professor);
            success = true;
        }
        finally
        {
            if (success)
                commitTransaction();
            else
                recoverableRollbackTransaction();
        }
    }

	/**
	 * delete professors in this collection
	 * @param professors professor collection
	 */
    public void deleteProfessors(Collection<? extends Professor> professors)
    {
        beginTransaction();
        boolean success = false;
        try
        {
            final Session session = getSession();
            professors.forEach(session::delete);
            success = true;
        }
        finally
        {
            if (success)
                commitTransaction();
            else
                recoverableRollbackTransaction();
        }
    }

    /**
     * Delete the specified user profiles.
     *
     * @param qlBuilder a QL builder that will return UserProfiles to delete.
     */
    public void deleteProfessors(QLBuilder qlBuilder)
    {
        beginTransaction();
        boolean success = false;
        try
        {
            final Session session = getSession();
            final QLResolver queryResolver = qlBuilder.getQueryResolver();
            final QLResolverOptions options = new QLResolverOptions();
            final int atATime = 200;
            options.setFetchSize(atATime);
            queryResolver.setOptions(options);
            try (CloseableIterator<Professor> it = queryResolver.iterate())
            {
                int count = 0;
                while (it.hasNext())
                {
                    Professor professor = it.next();
                    session.delete(professor);
                    if (++count > atATime)
                    {
                        count = 0;
                        session.flush(); // May need to clear action queues as well to free up memory.
                    }
                }
            }
            catch (Exception e)
            {
                throw new HibernateException("Unable to iterate over query results.", e);
            }
            success = true;
        }
        finally
        {
            if (success)
                commitTransaction();
            else
                recoverableRollbackTransaction();
        }
    }

    /**
     * Evict the specified entity from the session.
     *
     * @param entity the entity.
     */
    public void evict(Object entity)
    {
        getSession().evict(entity);
    }

    /**
     * Convert a URL to a URI returning the default value on error.
     *
     * @param url the URL.
     * @param defaultValue the default value.
     * @return the URI.
     */
    @Contract("_,null->null;_,!null->!null")
    public URI toURI(URL url, URI defaultValue)
    {
        if (url == null) return defaultValue;
        try
        {
            return url.toURI();
        }
        catch (URISyntaxException e)
        {
            _logger.warn("Unable to convert URL to URI: " + url, e);
        }
        return defaultValue;
    }

    /**
     * Convert a URL to a string if possible or return null.
     *
     * @param link the link.
     * @return the link as a String or null.
     */
    @Nullable
    @Contract("null->null;!null->!null")
    public String toString(@Nullable URL link)
    {
        return link == null ? null : link.toString();
    }

}
