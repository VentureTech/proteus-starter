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

package com.example.app.profile.ui.company;

import com.example.app.profile.model.ProfileDAO;
import com.example.app.profile.model.ProfileType;
import com.example.app.profile.model.company.Company;
import com.example.app.profile.model.company.CompanyDAO;
import com.example.app.profile.model.user.UserDAO;
import com.example.app.profile.service.SelectedCompanyTermProvider;
import com.example.app.profile.ui.ApplicationFunctions;
import com.example.app.resource.service.ResourceCategoryLabelProvider;
import com.example.app.resource.service.ResourceTagsLabelProvider;
import com.example.app.support.service.AppUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelPropertyEditor;

import net.proteusframework.cms.label.LabelDAO;
import net.proteusframework.cms.label.LabelDomain;
import net.proteusframework.cms.label.LabelDomainProvider;
import net.proteusframework.core.GloballyUniqueStringGenerator;
import net.proteusframework.core.StringFactory;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.ConcatTextSource;
import net.proteusframework.core.locale.LocalizedObjectKey;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.management.ParsedRequest;
import net.proteusframework.ui.management.nav.NavigationAction;
import net.proteusframework.ui.management.nav.config.CurrentURLPropertyValueResolver;
import net.proteusframework.ui.management.nav.config.PropertyValueResolverParameter;
import net.proteusframework.ui.miwt.ReflectiveAction;
import net.proteusframework.ui.miwt.component.Dialog;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.composite.editor.PropertyEditor;
import net.proteusframework.ui.miwt.event.ActionEvent;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.CommonButtonText;

import static com.example.app.profile.ui.UIText.RESOURCE;
import static com.example.app.profile.ui.URLProperties.COMPANY;
import static com.example.app.profile.ui.company.AbstractCompanyPropertyEditorLOK.*;
import static com.i2rd.miwt.util.CSSUtil.CSS_INSTRUCTIONS;

/**
 * Abstract {@link PropertyEditor} for {@link Company}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1 /3/17
 */
@I18NFile(
    symbolPrefix = "com.example.app.profile.ui.company.AbstractCompanyPropertyEditor",
    i18n = {
        @I18N(symbol = "Title Copy Categories and Tags FMT", l10n = @L10N("Copy {0} Categories and Tags?")),
        @I18N(symbol = "Label Copy Categories and Tags Question FMT", l10n = @L10N(
            "Would you like to copy a default set of {0} Categories and Tags into this new {1}?")),
        @I18N(symbol = "Instructions Copy Categories And Tags FMT", l10n = @L10N(
            "This will create a set of categories and tags that can be used when creating {0} for this {1}."))
    }
)
@Configurable
public abstract class AbstractCompanyPropertyEditor extends MIWTPageElementModelPropertyEditor<Company>
{
    /**
     * Enum defining edit modes for this PropertyEditor
     */
    public static enum EditMode
    {
        DefaultCompany,
        StandardCompany
    }

    @Autowired protected EntityRetriever _er;
    @Autowired protected UserDAO _userDAO;
    @Autowired protected CompanyDAO _companyDAO;
    @Autowired protected ProfileDAO _profileDAO;
    @Autowired protected SelectedCompanyTermProvider _terms;
    @Autowired protected ResourceCategoryLabelProvider _rclp;
    @Autowired protected ResourceTagsLabelProvider _rtlp;
    @Autowired protected LabelDAO _labelDAO;
    @Autowired protected AppUtil _appUtil;
    @Autowired protected CompanyUIPermissionCheck _permissionCheck;

    private Company _saved;
    private EditMode _editMode = EditMode.StandardCompany;

    /**
     * Instantiates a new company property editor.
     */
    public AbstractCompanyPropertyEditor()
    {
        super();
        addClassName("company-editor");
        setHTMLElement(HTMLElement.section);
    }

    @Override
    public CompanyValueEditor getValueEditor()
    {
        return (CompanyValueEditor)super.getValueEditor();
    }

    /**
     * Sets edit mode.
     *
     * @param editMode the edit mode
     */
    public void setEditMode(EditMode editMode)
    {
        _editMode = editMode;
    }

    /**
     * With edit mode abstract company property editor.
     *
     * @param editMode the edit mode
     *
     * @return the abstract company property editor
     */
    public AbstractCompanyPropertyEditor withEditMode(EditMode editMode)
    {
        setEditMode(editMode);
        return this;
    }

    /**
     * Sets saved.
     *
     * @param saved the saved
     */
    protected void setSaved(@Nullable Company saved)
    {
        _saved = _er.reattachIfNecessary(saved);
    }

    /**
     * Gets saved.
     *
     * @return the saved
     */
    protected Company getSaved()
    {
        return _er.reattachIfNecessary(_saved);
    }

    /**
     * Gets hostname setup.
     *
     * @return the hostname setup
     */
    protected abstract Consumer<Company> getHostnameSetup();

    /**
     * Gets post save.
     *
     * @return the post save
     */
    protected abstract Runnable getPostSave();

    /**
     * Gets post persist.
     *
     * @return the post persist
     */
    protected abstract Consumer<NavigationAction> getPostPersist();

    private void ensureValueEditorCreated()
    {
        if(getValueEditor() == null)
            setValueEditor(new CompanyValueEditor().withEditMode(_editMode));
    }

    @Override
    public void init()
    {
        super.init();
        ensureValueEditorCreated();

        final ReflectiveAction saveAction = CommonActions.SAVE.defaultAction();
        saveAction.setActionListener(ev -> {
            if(persist(company -> {
                final CompanyValueEditor editor = getValueEditor();
                assert company != null : "Company should not be null if you are persisting!";
                setProfileTypesIfNeeded(company);

                final LocalizedObjectKey companyName = company.getName();
                company.getRepository().setName(_appUtil.copyLocalizedObjectKey(companyName));
                Optional.ofNullable(company.getPrimaryLocation()).ifPresent(loc -> {
                    loc.setName(_appUtil.copyLocalizedObjectKey(companyName));
                    loc.getRepository().setName(_appUtil.copyLocalizedObjectKey(companyName));
                    loc.setProfileType(new ProfileType());
                    loc.getProfileType().setName(_appUtil.copyLocalizedObjectKey(companyName));
                    loc.getProfileType().setProgrammaticIdentifier(GloballyUniqueStringGenerator.getUniqueString());
                });

                getHostnameSetup().accept(company);

                if(StringFactory.isEmptyString(company.getProgrammaticIdentifier()))
                {
                    company.setProgrammaticIdentifier(GloballyUniqueStringGenerator.getUniqueString());
                }

                company = _companyDAO.saveCompany(company);

                if(editor.getWebLogoEditor().getModificationState().isModified())
                {
                    company = _companyDAO.saveCompanyImage(company, editor.getWebLogoEditor().commitValue());
                }
                if(editor.getEmailLogoEditor().getModificationState().isModified())
                {
                    company = _companyDAO.saveCompanyEmailLogo(company, editor.getEmailLogoEditor().commitValue());
                }
                _companyDAO.updateAdminsForCompany(company);
                setSaved(company);

                getPostSave().run();

                return Boolean.TRUE;
            }))
            {
                final NavigationAction action = CommonActions.SAVE.navAction();
                action.configure().toReturnPath(ApplicationFunctions.Company.MANAGEMENT)
                    .withSourceComponent(this);
                action.setPropertyValueResolver(new CurrentURLPropertyValueResolver(){
                    @Override
                    public Map<String, Object> resolve(PropertyValueResolverParameter parameter)
                    {
                        final Map<String, Object> map = super.resolve(parameter);
                        map.put(COMPANY, getSaved());
                        return map;
                    }
                });
                action.setTarget(this, "close");

                getPostPersist().accept(action);
            }
        });


        final NavigationAction cancelAction = CommonActions.CANCEL.navAction();
        cancelAction.configure().toReturnPath(ApplicationFunctions.Company.MANAGEMENT).usingCurrentURLData()
            .withSourceComponent(this);
        cancelAction.setTarget(this, "close");

        if(_editMode == EditMode.StandardCompany)
            setPersistenceActions(saveAction, cancelAction);
        else
            setPersistenceActions(saveAction);

        final boolean isNew = !Optional.ofNullable(getValueEditor().getValue())
            .filter(ce -> ce.getId() != null && ce.getId() > 0).isPresent();
        final TextSource editCompany = ConcatTextSource.create(
            isNew ? CommonButtonText.NEW : CommonButtonText.EDIT,
            _terms.company()).withSpaceSeparator();
        final TextSource companyName = Optional.ofNullable(getValueEditor().getValue())
            .filter(ce -> ce.getId() != null && ce.getId() > 0)
            .map(Company::getName)
            .map(name -> (TextSource)name)
            .orElse(CommonButtonText.NEW);
        moveToTop(of("text", new Label(isNew
            ? editCompany
            : ConcatTextSource.create(editCompany, companyName).withSeparator(": "))
            .withHTMLElement(HTMLElement.h1)));
    }

    /**
     * Prompt copy resource cats and tags.
     *
     * @param finalAction the final action
     */
    protected void promptCopyResourceCatsAndTags(NavigationAction finalAction)
    {
        final Company saved = getSaved();
        if(saved.getResourceCategories() != null && saved.getResourceTags() != null)
        {
            finalAction.actionPerformed(new ActionEvent(this, this, "done"));
            return;
        }
        //Prompt to ask if the user would like to copy Resource Tags and Categories.  If not, create new LabelDomains.
        final Dialog dlg = new Dialog(getApplication(), TITLE_COPY_CATEGORIES_AND_TAGS_FMT(RESOURCE()));

        final Label question = new Label(
            LABEL_COPY_CATEGORIES_AND_TAGS_QUESTION_FMT(RESOURCE(), _terms.company()));
        question.setHTMLElement(HTMLElement.h2);
        question.addClassName("prompt-question");

        final Label instructions = new Label(
            INSTRUCTIONS_COPY_CATEGORIES_AND_TAGS_FMT(RESOURCE(), _terms.company()));
        instructions.setHTMLElement(HTMLElement.div);
        instructions.addClassName(CSS_INSTRUCTIONS);

        final PushButton yes = CommonActions.YES.push();
        final PushButton no = CommonActions.NO.push();

        yes.addActionListener(ev -> {
            final LabelDomain rcld = new LabelDomain();
            rcld.setProgrammaticIdentifier("company-resourcecategories-" + saved.getId());
            rcld.setName(_rclp.getLabelDomain().getName().createCopy());
            _labelDAO.saveLabelDomain(rcld);
            copyLabelsFromLabelDomainProvider(_rclp, rcld);
            final LabelDomain rtld = new LabelDomain();
            rtld.setProgrammaticIdentifier("company-resourcetags-" + saved.getId());
            rtld.setName(_rtlp.getLabelDomain().getName().createCopy());
            _labelDAO.saveLabelDomain(rtld);
            copyLabelsFromLabelDomainProvider(_rtlp, rtld);

            saved.setResourceCategories(_er.reattachIfNecessary(rcld));
            saved.setResourceTags(_er.reattachIfNecessary(rtld));
            setSaved(_companyDAO.saveCompany(saved));
            dlg.close();
            finalAction.actionPerformed(ev);
        });

        no.addActionListener(ev -> {
            final LabelDomain rcld = new LabelDomain();
            rcld.setProgrammaticIdentifier("company-resourcecategories-" + saved.getId());
            rcld.setName(_rclp.getLabelDomain().getName().createCopy());
            _labelDAO.saveLabelDomain(rcld);
            final LabelDomain rtld = new LabelDomain();
            rtld.setProgrammaticIdentifier("company-resourcetags-" + saved.getId());
            rtld.setName(_rtlp.getLabelDomain().getName().createCopy());
            _labelDAO.saveLabelDomain(rtld);
            saved.setResourceCategories(_er.reattachIfNecessary(rcld));
            saved.setResourceTags(_er.reattachIfNecessary(rtld));
            setSaved(_companyDAO.saveCompany(saved));
            dlg.close();
            finalAction.actionPerformed(ev);
        });

        dlg.add(of("prompt", question, instructions, of("actions prompt-actions", yes, no)));

        getWindowManager().add(dlg);
        dlg.setVisible(true);
    }

    /**
     * Copy labels from label domain provider.
     *
     * @param provider the provider
     * @param newDomain the new domain
     */
    protected void copyLabelsFromLabelDomainProvider(LabelDomainProvider provider, LabelDomain newDomain)
    {
        provider.getLabels().forEach(label -> {
            final net.proteusframework.cms.label.Label newLabel = new net.proteusframework.cms.label.Label();
            newLabel.setEnabled(label.isEnabled());
            newLabel.setLabelDomain(newDomain);
            newLabel.setProgrammaticIdentifier(label.getProgrammaticIdentifier());
            newLabel.setName(_appUtil.copyLocalizedObjectKey(label.getName()));
            newLabel.setDescription(_appUtil.copyLocalizedObjectKey(label.getDescription()));
            _labelDAO.saveLabel(newLabel);
        });
    }

    @SuppressWarnings("ConstantConditions")
    private void setProfileTypesIfNeeded(Company company)
    {
        if(company.getProfileType() == null
           || company.getProfileType().getId() == null
           || company.getProfileType().getId() == 0)
        {
            company.setProfileType(_profileDAO.mergeProfileType(_companyDAO.createCompanyProfileType(null)));
        }
    }

    /**
     * Configure.
     *
     * @param request the request
     */
    @SuppressWarnings("unused") //Used by ApplicationFunction
    protected void configure(ParsedRequest request)
    {
        _permissionCheck.checkPermissionsForCurrent("You do not have the correct role to view this page");

        final Company val = request.getPropertyValue(COMPANY);
        if(val == null)
        {
            throw new IllegalArgumentException("Unable to determine Company.");
        }
        ensureValueEditorCreated();
        getValueEditor().setValue(val);
        setSaved(val);
    }
}
