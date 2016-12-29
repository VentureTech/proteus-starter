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

package com.example.app.model.client;

import com.example.app.model.profile.ProfileTypeProvider;
import com.example.app.model.user.UserDAO;
import com.example.app.support.AppUtil;
import com.example.app.support.FileSaver;
import org.apache.commons.fileupload.FileItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.function.BiFunction;

import com.i2rd.hibernate.util.HibernateUtil;

import net.proteusframework.cms.CmsSite;
import net.proteusframework.cms.FileSystemDirectory;
import net.proteusframework.core.hibernate.dao.DAOHelper;
import net.proteusframework.core.spring.ApplicationContextUtils;
import net.proteusframework.data.filesystem.DirectoryEntity;
import net.proteusframework.data.filesystem.FileEntity;
import net.proteusframework.data.filesystem.FileSystemDAO;

import static java.util.UUID.randomUUID;

/**
 * {@link DAOHelper} implementation for {@link Client}
 *
 * @author Ken Logan (klogan@venturetech.net)
 */
@Repository
public final class ClientDAO extends DAOHelper implements Serializable
{
    /** the logo file name suffix */
    public static final String LOGO_FILE_NAME_SUFFIX = "_logo";
    /**
     * Serialization ID. {@code ClientDAO} is {@code Serializable} and implements {@link #readResolve()} so that a
     * {@code Serializable} class can have it as an autowired field without worrying about it getting rewired properly when it is
     * deserialized. All properties of this class should be {@code transient} since they will never actually be deserialized.
     */
    private static final long serialVersionUID = -1864046652465972120L;
    /** The name of the folder to store logos in */
    private static final String LOGO_FOLDER = "Logos";
    private final HibernateUtil _hu = HibernateUtil.getInstance();
    @Autowired
    private transient LocationDAO _locationDAO;
    @Autowired
    private transient UserDAO _userDAO;
    @Autowired
    private transient ProfileTypeProvider _profileTypeProvider;
    @Autowired
    private transient FileSystemDAO _fileSystemDAO;
    @Autowired
    private transient AppUtil _appUtil;

    /**
     * Get the Client whose ID corresponds to the given ID
     *
     * @param id the ID to look for
     *
     * @return the matching Client, or null if none exists
     */
    @Nullable
    public Client getClient(@Nullable Integer id)
    {
        if (id == null || id == 0L) return null;
        return (Client) getSession().get(Client.class, id);
    }

    /**
     * Get the client logo directory for a site.
     *
     * @param site the operational site
     * @param client the client.
     *
     * @return the directory to store logos in
     */
    public DirectoryEntity getLogoDirectory(final CmsSite site, final Client client)
    {
        // FUTURE : I'd like to break up client pictures into buckets
        return _fileSystemDAO.mkdirs(FileSystemDirectory.Pictures.getDirectory2(site), null, LOGO_FOLDER);
    }

    /**
     * Save the given Client into the database by merging it
     *
     * @param client the client to save
     *
     * @return the persisted client
     */
    public Client mergeClient(Client client)
    {
        return (Client) doInTransaction(session -> {

            if (client.getProfileType() == null)
                client.setProfileType(_profileTypeProvider.company());

            return session.merge(client);
        });
    }

    /**
     * Save an image to the specified directory and set it as the client's logo.
     *
     * @param client the client to update
     * @param parent the parent directory
     * @param image the image to save
     */
    public void saveLogo(final Client client, final DirectoryEntity parent, final FileItem image)
    {
        _createImageSaver(parent).save(client, image);
    }

    private FileSaver<Client> _createImageSaver(final DirectoryEntity parent)
    {
        final BiFunction<Client, FileEntity, Client> setImage = (company, file) -> {
            company.setLogo(file);
            return company;
        };

        return new FileSaver<>(
            setImage, Client::getLogo, (company, image) -> parent, this::_getImageName, company -> company);
    }

    private String _getImageName(final Client client, final FileItem image)
    {
        return String.format(
            "%s%s%s", _hu.isTransient(client) ? randomUUID() : client.getId(),
            LOGO_FILE_NAME_SUFFIX, AppUtil.getExtensionWithDot(image));
    }

    private Object readResolve() throws ObjectStreamException
    {
        ApplicationContext context = ApplicationContextUtils.getInstance().getContext();
        assert context != null;
        return context.getBean(ClientDAO.class);
    }
}
