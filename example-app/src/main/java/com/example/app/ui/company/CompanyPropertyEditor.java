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

package com.example.app.ui.company;

import com.example.app.model.company.Company;
import com.example.app.model.company.CompanyDAO;
import com.example.app.model.company.SelectedCompanyTermProvider;
import com.example.app.model.profile.ProfileDAO;
import com.example.app.model.user.User;
import com.example.app.model.user.UserDAO;
import com.example.app.service.ResourceCategoryLabelProvider;
import com.example.app.service.ResourceTagsLabelProvider;
import com.example.app.support.AppUtil;
import com.example.app.support.Functions;
import com.example.app.ui.Application;
import com.example.app.ui.ApplicationFunctions;
import com.example.app.ui.URLConfigurations;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

import com.i2rd.cms.HostnameDestination;
import com.i2rd.cms.PageTitleAffixOption;
import com.i2rd.cms.SiteSSLOption;
import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelPropertyEditor;

import net.proteusframework.cms.CmsSite;
import net.proteusframework.cms.category.CmsCategory;
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
import net.proteusframework.ui.management.ApplicationFunction;
import net.proteusframework.ui.management.ParsedRequest;
import net.proteusframework.ui.management.URLConfigDef;
import net.proteusframework.ui.management.URLProperty;
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
import net.proteusframework.users.model.AuthenticationDomain;
import net.proteusframework.users.model.Principal;
import net.proteusframework.users.model.dao.PrincipalDAO;

import static com.example.app.ui.UIText.RESOURCE;
import static com.example.app.ui.URLProperties.COMPANY;
import static com.example.app.ui.URLProperties.COMPANY_PATH_INFO;
import static com.example.app.ui.company.CompanyPropertyEditorLOK.*;
import static com.i2rd.miwt.util.CSSUtil.CSS_INSTRUCTIONS;

/**
 * {@link PropertyEditor} implementation for {@link Company}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 6/27/16 1:04 PM
 */
@I18NFile(
    symbolPrefix = "com.lrsuccess.ldp.ui.coaching.CompanyPropertyEditor",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("Company Editor")),
        @I18N(symbol = "Title Copy Categories and Tags FMT", l10n = @L10N("Copy {0} Categories and Tags?")),
        @I18N(symbol = "Label Copy Categories and Tags Question FMT", l10n = @L10N(
            "Would you like to copy a default set of {0} Categories and Tags into this new {1}?")),
        @I18N(symbol = "Instructions Copy Categories And Tags FMT", l10n = @L10N(
            "This will create a set of categories and tags that can be used when creating {0} for this {1}."))
    }
)
@ApplicationFunction(
    applicationName = Application.NAME,
    sessionName = Application.SESSION,
    name = ApplicationFunctions.Company.EDIT,
    description = "Editor for Company",
    urlConfigDef = @URLConfigDef(
        name = URLConfigurations.Company.EDIT,
        properties = @URLProperty(name = COMPANY, type = Company.class),
        pathInfoPattern = COMPANY_PATH_INFO
    )
)
public class CompanyPropertyEditor extends MIWTPageElementModelPropertyEditor<Company>
{
    @Autowired private PrincipalDAO _principalDAO;
    @Autowired private EntityRetriever _er;
    @Autowired private UserDAO _userDAO;
    @Autowired private CompanyDAO _companyDAO;
    @Autowired private ProfileDAO _profileDAO;
    @Autowired private SelectedCompanyTermProvider _terms;
    @Autowired private ResourceCategoryLabelProvider _rclp;
    @Autowired private ResourceTagsLabelProvider _rtlp;
    @Autowired private LabelDAO _labelDAO;
    @Autowired private AppUtil _appUtil;

    private Company _saved;

    /**
     * Instantiates a new company property editor.
     */
    public CompanyPropertyEditor()
    {
        super(new CompanyValueEditor());
        setName(COMPONENT_NAME());
        addCategory(CmsCategory.ClientBackend);
        addClassName("company-editor");
        setHTMLElement(HTMLElement.section);
    }

    @Override
    public CompanyValueEditor getValueEditor()
    {
        return (CompanyValueEditor)super.getValueEditor();
    }

    /**
     * Sets saved.
     *
     * @param saved the saved
     */
    void setSaved(@Nullable Company saved)
    {
        _saved = _er.reattachIfNecessary(saved);
    }

    /**
     * Gets saved.
     *
     * @return the saved
     */
    Company getSaved()
    {
        return _er.reattachIfNecessary(_saved);
    }

    @Override
    public void init()
    {
        super.init();

        final ReflectiveAction saveAction = CommonActions.SAVE.defaultAction();
        saveAction.setActionListener(ev -> {
            if(persist(company -> {
                final CompanyValueEditor editor = getValueEditor();
                assert company != null : "Company should not be null if you are persisting!";
                setProfileTypesIfNeeded(company);

                company.getRepository().setName(_appUtil.copyLocalizedObjectKey(company.getName()));

                if(_companyDAO.isTransient(company) || company.getHostname().getDomain() == null)
                {
                    final AuthenticationDomain authDomain = new AuthenticationDomain();
                    authDomain.setDomainName(company.getHostname().getName());
                    company.getHostname().setDomain(authDomain);
                    company.getHostname().setDestination(HostnameDestination.welcome_page);
                    company.getHostname().setPageTitleAffix(LocalizedObjectKey.copyLocalizedObjectKey(
                        getLocaleContext().getLocaleSource(), company.getName()));
                    company.getHostname().setPageTitleAffixOption(PageTitleAffixOption.Prefix);
                    final CmsSite site = _appUtil.getSite();
                    company.getHostname().setSite(site);
                    company.getHostname().setSslOption(SiteSSLOption.no_influence);
                    company.getHostname().setWelcomePage(site.getDefaultHostname().getWelcomePage());
                }

                if(StringFactory.isEmptyString(company.getProgrammaticIdentifier()))
                {
                    company.setProgrammaticIdentifier(GloballyUniqueStringGenerator.getUniqueString());
                }

                company = _companyDAO.mergeCompany(company);

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

                promptCopyResourceCatsAndTags(action);
            }
        });


        final NavigationAction cancelAction = CommonActions.CANCEL.navAction();
        cancelAction.configure().toReturnPath(ApplicationFunctions.Company.MANAGEMENT).usingCurrentURLData()
            .withSourceComponent(this);
        cancelAction.setTarget(this, "close");

        setPersistenceActions(saveAction, cancelAction);

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

    private void promptCopyResourceCatsAndTags(NavigationAction finalAction)
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

            saved.setResourceCategories(rcld);
            saved.setResourceTags(rtld);
            setSaved(_companyDAO.mergeCompany(saved));
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
            saved.setResourceCategories(rcld);
            saved.setResourceTags(rtld);
            setSaved(_companyDAO.mergeCompany(saved));
            dlg.close();
            finalAction.actionPerformed(ev);
        });

        dlg.add(of("prompt", question, instructions, of("actions prompt-actions", yes, no)));

        getWindowManager().add(dlg);
        dlg.setVisible(true);
    }

    private void copyLabelsFromLabelDomainProvider(LabelDomainProvider provider, LabelDomain newDomain)
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

    @SuppressWarnings("unused") //Used by ApplicationFunction
    void configure(ParsedRequest request)
    {
        final User currentUser = _userDAO.getCurrentUser();
        final Principal currentPrincipal = _principalDAO.getCurrentPrincipal();
        if(currentPrincipal == null
           || !Optional.ofNullable(currentUser).map(AppUtil::userHasAdminRole).orElse(AppUtil.userHasAdminRole(currentPrincipal)))
            throw new IllegalArgumentException(String.format("User %s does not have the correct role to view this page",
                Functions.orElseFlatMap(Optional.ofNullable(currentUser).map(User::getId),
                    () -> Optional.ofNullable(currentPrincipal).map(Principal::getId).map(Long::intValue)).orElse(0)));

        final Company val = request.getPropertyValue(COMPANY);
        if(val == null)
        {
            throw new IllegalArgumentException("Unable to determine Company.");
        }

        getValueEditor().setValue(val);
        setSaved(val);
    }
}
