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

package com.example.app.model.company;

import com.example.app.model.SoftDeleteEntity;
import com.example.app.model.profile.Profile;
import com.example.app.model.profile.ProfileTypeProvider;
import com.example.app.model.user.UserDAO;
import com.example.app.support.AppUtil;
import com.example.app.support.ImageSaver;
import com.google.common.base.Preconditions;
import org.apache.commons.fileupload.FileItem;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.function.BiFunction;

import com.i2rd.hibernate.util.HibernateUtil;

import net.proteusframework.cms.CmsSite;
import net.proteusframework.cms.FileSystemDirectory;
import net.proteusframework.core.hibernate.dao.DAOHelper;
import net.proteusframework.core.spring.ApplicationContextUtils;
import net.proteusframework.data.filesystem.DirectoryEntity;
import net.proteusframework.data.filesystem.FileEntity;
import net.proteusframework.data.filesystem.FileSystemDAO;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.QLBuilderImpl;

import static com.example.app.model.company.Location.COMPANY_PROP;
import static java.util.UUID.randomUUID;
import static net.proteusframework.ui.search.PropertyConstraint.Operator.in;
import static net.proteusframework.ui.search.PropertyConstraint.Operator.not_in;

/**
 * {@link DAOHelper} implementation for {@link Company}
 *
 * @author Ken Logan (klogan@venturetech.net)
 */
@Repository
public final class CompanyDAO extends DAOHelper implements Serializable
{
    /**
     * Serialization ID. {@code CompanyDAO} is {@code Serializable} and implements {@link #readResolve()} so that a
     * {@code Serializable} class can have it as an autowired field without worrying about it getting rewired properly when it is
     * deserialized. All properties of this class should be {@code transient} since they will never actually be deserialized.
     */
    private static final long serialVersionUID = -1864046652465972120L;

    /** The name of the folder to store logos in */
    private static final String LOGO_FOLDER = "Logos";

    /** the logo file name suffix */
    public static final String LOGO_FILE_NAME_SUFFIX = "_logo";

    @Autowired private transient LocationDAO _locationDAO;
    @Autowired private transient UserDAO _userDAO;
    @Autowired private transient ProfileTypeProvider _profileTypeProvider;
    @Autowired private transient FileSystemDAO _fileSystemDAO;
    @Autowired private transient AppUtil _appUtil;

    private final HibernateUtil _hu = HibernateUtil.getInstance();

    /**
     *   Save the given Company into the database by merging it
     *   @param company the company to save
     *   @return the persisted company
     */
    public Company mergeCompany(Company company)
    {
        return (Company) doInTransaction(session -> {

            if(company.getProfileType() == null)
                company.setProfileType(_profileTypeProvider.company());

            return session.merge(company);
        });
    }

    /**
     *   Delete the specified Companies and their Locations and Users from the database
     *   @param companies the companies to delete
     */
    // FIXME : implement
/*    public void deleteCompanies(Collection<? extends Company> companies)
    {
        doInTransaction(session -> {
            _locationDAO.deleteAllLocations(companies);
            _userDAO.deleteAllUsers(companies);

            final String hql = "update " + Company.class.getName() + " company"
                + " set company." + SoftDeleteEntity.SOFT_DELETE_COLUMN_PROP + " = true"
                + " where company." + Profile.ID_PROP + " in :companies";

            final Query query = session.createQuery(hql);
            query.setParameterList("companies", companies.stream().map(Company::getId).toArray(Integer[]::new));

            query.executeUpdate();
        });
    }*/

    /**
     * Set the company status. If set to {@link CompanyStatus#INACTIVE} all of the company's users will be suspended. If set to
     * {@link CompanyStatus#ACTIVE} any suspended users will be reactivated.
     *
     * @param companies the companies to update
     * @param status the new status
     */
    // FIXME : implement
//    public void setCompanyStatus(final Collection<? extends Company> companies, final CompanyStatus status)
//    {
//        doInTransaction(session -> {
//            switch (status)
//            {
//                case PENDING:
//                    // Nothing to do
//                    break;
//                case INACTIVE:
//                    _userDAO.suspendAllUsers(companies);
//                    break;
//                case ACTIVE:
//                    _userDAO.reactivateAllUsers(companies);
//                    break;
//                default:
//                    throw new IllegalArgumentException("Unsupported company status: " + status.getName());
//            }
//
//            final String hql = "update " + Company.class.getName() + " company"
//                + " set company." + Company.STATUS_PROP + " = :status"
//                + " where company." + Profile.ID_PROP + " in :companies";
//
//            final Query query = session.createQuery(hql);
//            query.setParameter("status", status);
//            query.setParameterList("companies", companies.stream().map(Company::getId).toArray(Integer[]::new));
//
//            query.executeUpdate();
//        });
//    }


    /**
     *   Get the Company whose ID corresponds to the given ID
     *   @param id the ID to look for
     *   @return the matching Company, or null if none exists
     */
    @Nullable
    public Company getCompany(@Nullable Integer id)
    {
        if(id == null || id == 0L) return null;
        return (Company) getSession().get(Company.class, id);
    }

    /**
     * Get the company logo directory for a site.
     *
     * @param site the operational site
     * @param company the company.
     * @return the directory to store logos in
     */
    public DirectoryEntity getLogoDirectory(final CmsSite site, final Company company)
    {
        // FIXME : I'd like to break up company pictures into buckets
        return _fileSystemDAO.mkdirs(FileSystemDirectory.Pictures.getDirectory2(site), null, LOGO_FOLDER);
    }

    /**
     * Save an image to the specified directory and set it as the company's logo.
     *
     * @param company the company to update
     * @param parent the parent directory
     * @param image the image to save
     */
    public void saveLogo(final Company company, final DirectoryEntity parent, final FileItem image)
    {
        _createImageSaver(parent).saveImage(company, image);
    }

    private ImageSaver<Company> _createImageSaver(final DirectoryEntity parent)
    {
        final BiFunction<Company, FileEntity, Company> setImage = (company, file) -> {
            company.setLogo(file);
            return company;
        };

        return new ImageSaver<>(
            setImage, Company::getLogo, (company, image) -> parent, this::_getImageName, company -> company);
    }

    private String _getImageName(final Company company, final FileItem image)
    {
        return String.format(
            "%s%s%s", _hu.isTransient(company)? randomUUID() : company.getId(),
            LOGO_FILE_NAME_SUFFIX, AppUtil.getExtensionWithDot(image));
    }


    /** @return the single instance of {@code CompanyDAO} from the {@link ApplicationContext} */
    private Object readResolve() throws ObjectStreamException
    {
        ApplicationContext context = ApplicationContextUtils.getInstance().getContext();
        assert context != null;
        return context.getBean(CompanyDAO.class);
    }
}
