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

package com.example.app.profile.model.location;

import com.example.app.profile.model.ProfileTypeProvider;
import com.example.app.profile.model.client.Client;
import com.example.app.profile.model.client.ClientStatus;
import com.example.app.support.service.AppUtil;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

import net.proteusframework.core.hibernate.dao.DAOHelper;
import net.proteusframework.core.hibernate.model.SoftDeleteEntity;
import net.proteusframework.core.spring.ApplicationContextUtils;

/**
 * {@link DAOHelper} implementation for {@link Location}
 *
 * @author Ken Logan (klogan@venturetech.net)
 */
@Repository
public final class LocationDAO extends DAOHelper implements Serializable
{
    /**
     * Serialization ID. {@code LocationDAO} is {@code Serializable} and implements {@link #readResolve()} so that a
     * {@code Serializable} class can have it as an autowired field without worrying about it getting rewired properly when it is
     * deserialized. All properties of this class should be {@code transient} since they will never actually be deserialized.
     */
    private static final long serialVersionUID = -1864046652465972120L;

    @Autowired private transient ProfileTypeProvider _profileTypeProvider;
    @Autowired private transient AppUtil _appUtil;

    /**
     * Delete the given Locations from the database
     *
     * @param locations the locations to delete
     */
    public void deleteLocations(final Collection<? extends Location> locations)
    {
        doInTransaction(session -> {
            final String hql = "update " + Location.class.getSimpleName() + " location"
                               + " set location." + SoftDeleteEntity.SOFT_DELETE_COLUMN_PROP + " = true"
                               + " where location in :locations";

            session.createQuery(hql)
                .setParameterList("locations", locations.stream().map(Location::getId).toArray(Integer[]::new))
                .executeUpdate();
        });
    }

    /**
     * Get the active locations for a client.
     *
     * @param client the client
     *
     * @return the locations, sorted by name
     */
    @NotNull
    public List<Location> getActiveLocations(@NotNull Client client)
    {
        Query q = getSession().createQuery("from " + Location.class.getSimpleName()
                                           + " l where l.company = :company and l.status = :active order by getText(l.name)");
        q.setParameter("company", client);
        q.setParameter("active", ClientStatus.ACTIVE);

        @SuppressWarnings("unchecked")
        final List<Location> location = (List<Location>) q.list();
        return location;
    }

    /**
     * Get the Location whose ID corresponds to the given ID
     *
     * @param id the ID to look for
     *
     * @return the matching Location, or null of none exists
     */
    @Nullable
    public Location getLocation(@Nullable Long id)
    {
        if (id == null || id == 0L) return null;
        return (Location) getSession().get(Location.class, id);
    }

    /**
     * Get all the locations for a client.
     *
     * @param client the client
     *
     * @return the locations, sorted by name
     */
    @NotNull
    public List<Location> getLocations(@NotNull Client client)
    {
        Query q = getSession().createQuery("from " + Location.class.getSimpleName()
                                           + " l where l.company = :company order by getText(l.name)");
        q.setParameter("company", client);

        @SuppressWarnings("unchecked")
        final List<Location> location = (List<Location>) q.list();
        return location;
    }

    /**
     * Check if a client has active locations.
     *
     * @param client the client
     *
     * @return true if the client has at least one active location
     */
    public boolean hasActiveLocations(final Client client)
    {
        Query q = getSession().createQuery("select count(*) > 0 from " + Location.class.getSimpleName()
                                           + " l where l.company = :company and l.status = :active");
        q.setParameter("company", client);
        q.setParameter("active", ClientStatus.ACTIVE);

        @SuppressWarnings("unchecked")
        final List<Boolean> location = (List<Boolean>) q.list();
        return location.get(0);
    }

    /**
     * Save the given Location into the database
     *
     * @param location the location to save
     *
     * @return the location
     */
    public Location saveLocation(Location location)
    {
        BiConsumer<Location, Session> presave = (toSave, session) -> {
            if(toSave.getProfileType() == null)
                toSave.setProfileType(_profileTypeProvider.location());
            if(toSave.getRepository().getId() == null || toSave.getRepository().getId() == 0)
                toSave.getRepository().setName(_appUtil.copyLocalizedObjectKey(toSave.getName()));
        };
        if(isAttached(location))
        {
            return doInTransaction(session -> {
                presave.accept(location, session);
                session.saveOrUpdate(location);
                return location;
            });
        }
        else
        {
            return doInTransaction(session -> {
                presave.accept(location, session);
                return (Location)session.merge(location);
            });
        }
    }

    /**
     * Set the status of the specified locations.
     *
     * @param locations the locations to update
     * @param status the new status
     */
    public void setLocationStatus(final Collection<? extends Location> locations, final LocationStatus status)
    {
        doInTransaction(session -> {
            final String hql = "update " + Location.class.getName() + " location"
                               + " set location." + Location.STATUS_PROP + " = :status"
                               + " where location in :locations";

            final Query query = session.createQuery(hql);
            query.setParameter("status", status);
            query.setParameterList("locations", locations.stream().map(Location::getId).toArray(Integer[]::new));

            query.executeUpdate();
        });
    }

    private Object readResolve() throws ObjectStreamException
    {
        ApplicationContext context = ApplicationContextUtils.getInstance().getContext();
        assert context != null;
        return context.getBean(LocationDAO.class);
    }
}
