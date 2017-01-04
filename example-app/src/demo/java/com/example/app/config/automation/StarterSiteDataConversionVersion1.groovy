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

package com.example.app.config.automation

import net.proteusframework.core.automation.DataConversion
import net.proteusframework.core.automation.SQLDataConversion
import net.proteusframework.core.automation.TaskQualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

/**
 * Data conversions for starter site data model
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/30/16
 */
@Configuration
@Lazy
class StarterSiteDataConversionVersion1
{
    private static final String IDENTIFIER = "starter-site"

    /**
     * initial data conversion
     * 2016.12.30 at 19:07 UTC
     * @return Bean.
     */
    @TaskQualifier(TaskQualifier.Type.data_conversion)
    @Bean
    DataConversion dataConversion_201612301907()
    {
        def ddl = [
            $/CREATE SCHEMA app/$,
            $/CREATE SCHEMA audit/$,
            $/create table app.Client (status varchar(255) not null, profile_id int4 not null, 
logo_id int8, location_id int4, primary key (profile_id))/$,
            $/create table app.Client_Location (profile_id int4 not null, location_id int4 not null)/$,
            $/create table app.Company (facebookLink varchar(255), googlePlusLink varchar(255), linkedInLink varchar(255), 
programmaticIdentifier varchar(255) not null, status varchar(255) not null, twitterLink varchar(255), 
websiteLink varchar(255), profile_id int4 not null, emaillogo_id int8, hostname_id int8 not null, image_id int8, 
location_id int4, profileTerms_id int4 not null, resourceCategories_id int8, resourceTags_id int8, primary key (profile_id))/$,
            $/create table app.Company_Location (profile_id int4 not null, location_id int4 not null)/$,
            $/create table app.Location (status varchar(255) not null, profile_id int4 not null, address_id int8, 
emailaddress_id int8, phonenumber_id int8, primary key (profile_id))/$,
            $/create table app.Membership (membership_id int4 not null, createtime timestamp not null, 
lastmodtime timestamp not null, enddate timestamp, startdate timestamp, createuser_id int8, lastmoduser_id int8, 
membershiptype_id int4, profile_id int4, user_id int4 not null, primary key (membership_id))/$,
            $/create table app.MembershipOperation (membershipOperation_id int4 not null, name int8 not null, 
programmaticIdentifier varchar(255) not null, primary key (membershipOperation_id))/$,
            $/create table app.MembershipType (membershiptype_id int4 not null, createtime timestamp not null, 
lastmodtime timestamp not null, description int8, name int8 not null, programmaticIdentifier varchar(255) not null, 
createuser_id int8, lastmoduser_id int8, profiletype_id int4 not null, primary key (membershiptype_id))/$,
            $/create table app.Note (note_id int4 not null, createtime timestamp not null, lastmodtime timestamp not null, 
content varchar(4096) not null, createuser_id int8, lastmoduser_id int8, primary key (note_id))/$,
            $/create table app.Profile (disc_type varchar(31) not null, profile_id int4 not null, createtime timestamp not null, 
lastmodtime timestamp not null, description int8, name int8 not null, createuser_id int8, lastmoduser_id int8, 
parent int4, profiletype_id int4 not null, repository_id int4 not null, primary key (profile_id))/$,
            $/create table app.ProfileDatedRecord (profileDatedRecord_id int4 not null, category varchar(255) not null, 
date timestamp not null, subCategory varchar(255), profile_id int4 not null, primary key (profileDatedRecord_id))/$,
            $/create table app.ProfileType (disc_type varchar(31) not null, profiletype_id int4 not null, 
createtime timestamp not null, lastmodtime timestamp not null, description int8, name int8 not null, 
programmaticIdentifier varchar(255) not null, createuser_id int8, lastmoduser_id int8, 
kind_id int8, primary key (profiletype_id))/$,
            $/create table app.Schedule (schedule_id int4 not null, disc_type varchar(255) not null, 
createtime timestamp not null, createuser int8, lastmodtime timestamp not null, lastmoduser int8, primary key (schedule_id))/$,
            $/create table app.User (user_id int4 not null, createtime timestamp not null, lastmodtime timestamp not null, 
facebookLink varchar(255), googlePlusLink varchar(255), linkedInLink varchar(255), preferredContactMethod varchar(255), 
twitterLink varchar(255), createuser_id int8, lastmoduser_id int8, image_id int8, principal_id int8 not null, 
smsPhone_id int8, primary key (user_id))/$,
            $/create table app.UserPosition (userPosition_id int4 not null, createtime timestamp not null, 
lastmodtime timestamp not null, current boolean not null, endDate timestamp, position varchar(255) not null, 
startDate timestamp, createuser_id int8, lastmoduser_id int8, user_id int4 not null, primary key (userPosition_id))/$,
            $/create table app.company_user (profile_id int4 not null, user_id int4 not null)/$,
            $/create table app.ical4jschedule (ICal4jSchedule_id int4 not null, eventprogrammaticidentifier varchar(255), 
repeat boolean, recurrencerule varchar(255), temporaldirection varchar(255), primary key (ICal4jSchedule_id))/$,
            $/create table app.membership_operations (membership_id int4 not null, membershipOperation_id int4 not null)/$,
            $/create table app.membershiptype_operations (membershiptype_id int4 not null, membershipOperation_id int4 not null)/$,
            $/create table app.profileterms (profileterms_id int4 not null, coachingentities int8, 
coachingentity int8, primary key (profileterms_id))/$,
            $/create table app.relativeperiodschedule (RelativePeriodSchedule_id int4 not null, 
eventprogrammaticidentifier varchar(255), repeat boolean, period varchar(255), time time, 
temporaldirection varchar(255), primary key (RelativePeriodSchedule_id))/$,
            $/create table app.repository (repository_id int4 not null, createtime timestamp not null, 
lastmodtime timestamp not null, description int8, name int8 not null, createuser_id int8, 
lastmoduser_id int8, primary key (repository_id))/$,
            $/create table app.repositoryItem (disc_type varchar(31) not null, repositoryItem_id int4 not null, 
createtime timestamp not null, lastmodtime timestamp not null, source varchar(255) not null, 
status varchar(255) not null, createuser_id int8, lastmoduser_id int8, primary key (repositoryItem_id))/$,
            $/create table app.repositoryItemRelation (repositoryItemRelation_id int4 not null, 
createtime timestamp not null, lastmodtime timestamp not null, relationType varchar(255), createuser_id int8, 
lastmoduser_id int8, repository_id int4 not null, repositoryItem_id int4 not null, primary key (repositoryItemRelation_id))/$,
            $/create table app.resource (disc_type varchar(31) not null, resource_id int4 not null, 
createtime timestamp not null, lastmodtime timestamp not null, author varchar(255), description int8, 
name int8 not null, resourceType varchar(255) not null, visibility varchar(255) not null, uri bytea, 
createuser_id int8, lastmoduser_id int8, category_id int8, image_id int8, file_id int8, primary key (resource_id))/$,
            $/create table app.resourceRepositoryItem (repositoryItem_id int4 not null, 
resource_id int4 not null, primary key (repositoryItem_id))/$,
            $/create table app.resource_tags (resource_id int4 not null, tag_id int8 not null)/$,
            $/create table audit.Client_AUD (profile_id int4 not null, REV int4 not null, status varchar(255), 
logo_id int8, location_id int4, primary key (profile_id, REV))/$,
            $/create table audit.Client_Location_AUD (REV int4 not null, profile_id int4 not null, 
location_id int4 not null, revtype int2, primary key (REV, profile_id, location_id))/$,
            $/create table audit.Company_AUD (profile_id int4 not null, REV int4 not null, facebookLink varchar(255), 
googlePlusLink varchar(255), linkedInLink varchar(255), programmaticIdentifier varchar(255), status varchar(255), 
twitterLink varchar(255), websiteLink varchar(255), emaillogo_id int8, hostname_id int8, image_id int8, 
location_id int4, profileTerms_id int4, resourceCategories_id int8, resourceTags_id int8, primary key (profile_id, REV))/$,
            $/create table audit.Company_Location_AUD (REV int4 not null, profile_id int4 not null, 
location_id int4 not null, revtype int2, primary key (REV, profile_id, location_id))/$,
            $/create table audit.Location_AUD (profile_id int4 not null, REV int4 not null, status varchar(255), 
address_id int8, emailaddress_id int8, phonenumber_id int8, primary key (profile_id, REV))/$,
            $/create table audit.MembershipType_AUD (membershiptype_id int4 not null, REV int4 not null, revtype int2, 
description int8, name int8, programmaticIdentifier varchar(255), profiletype_id int4, primary key (membershiptype_id, REV))/$,
            $/create table audit.Membership_AUD (membership_id int4 not null, REV int4 not null, revtype int2, 
enddate timestamp, startdate timestamp, membershiptype_id int4, profile_id int4, user_id int4, primary key (membership_id, REV))/$,
            $/create table audit.Note_AUD (note_id int4 not null, REV int4 not null, revtype int2, 
content varchar(4096), primary key (note_id, REV))/$,
            $/create table audit.ProfileType_AUD (disc_type varchar(31) not null, profiletype_id int4 not null, 
REV int4 not null, revtype int2, description int8, name int8, programmaticIdentifier varchar(255), 
kind_id int8, primary key (profiletype_id, REV))/$,
            $/create table audit.Profile_AUD (disc_type varchar(31) not null, profile_id int4 not null, 
REV int4 not null, revtype int2, description int8, name int8, parent int4, profiletype_id int4, 
repository_id int4, primary key (profile_id, REV))/$,
            $/create table audit.REVINFO (REV int4 not null, REVTSTMP int8, primary key (REV))/$,
            $/create table audit.Schedule_AUD (disc_type varchar(255) not null, schedule_id int4 not null, 
REV int4 not null, revtype int2, primary key (schedule_id, REV))/$,
            $/create table audit.UserPosition_AUD (userPosition_id int4 not null, REV int4 not null, 
revtype int2, current boolean, endDate timestamp, position varchar(255), startDate timestamp, 
user_id int4, primary key (userPosition_id, REV))/$,
            $/create table audit.User_AUD (user_id int4 not null, REV int4 not null, revtype int2, 
facebookLink varchar(255), googlePlusLink varchar(255), linkedInLink varchar(255), preferredContactMethod varchar(255), 
twitterLink varchar(255), image_id int8, principal_id int8, smsPhone_id int8, primary key (user_id, REV))/$,
            $/create table audit.company_user_AUD (REV int4 not null, profile_id int4 not null, 
user_id int4 not null, revtype int2, primary key (REV, profile_id, user_id))/$,
            $/create table audit.ical4jschedule_AUD (ICal4jSchedule_id int4 not null, REV int4 not null, 
eventprogrammaticidentifier varchar(255), repeat boolean, recurrencerule varchar(255), temporaldirection int4, 
primary key (ICal4jSchedule_id, REV))/$,
            $/create table audit.membership_operations_AUD (REV int4 not null, membership_id int4 not null, 
membershipOperation_id int4 not null, revtype int2, primary key (REV, membership_id, membershipOperation_id))/$,
            $/create table audit.membershiptype_operations_AUD (REV int4 not null, membershiptype_id int4 not null, 
membershipOperation_id int4 not null, revtype int2, primary key (REV, membershiptype_id, membershipOperation_id))/$,
            $/create table audit.profileterms_AUD (profileterms_id int4 not null, REV int4 not null, revtype int2, 
coachingentities int8, coachingentity int8, primary key (profileterms_id, REV))/$,
            $/create table audit.relativeperiodschedule_AUD (RelativePeriodSchedule_id int4 not null, REV int4 not null, 
eventprogrammaticidentifier varchar(255), repeat boolean, period varchar(255), time time, 
temporaldirection int4, primary key (RelativePeriodSchedule_id, REV))/$,
            $/create table audit.repositoryItemRelation_AUD (repositoryItemRelation_id int4 not null, 
REV int4 not null, revtype int2, relationType varchar(255), repository_id int4, 
repositoryItem_id int4, primary key (repositoryItemRelation_id, REV))/$,
            $/create table audit.repositoryItem_AUD (disc_type varchar(31) not null, repositoryItem_id int4 not null, 
REV int4 not null, revtype int2, source varchar(255), status varchar(255), primary key (repositoryItem_id, REV))/$,
            $/create table audit.repository_AUD (repository_id int4 not null, REV int4 not null, revtype int2, 
description int8, name int8, primary key (repository_id, REV))/$,
            $/create table audit.resourceRepositoryItem_AUD (repositoryItem_id int4 not null, REV int4 not null, 
resource_id int4, primary key (repositoryItem_id, REV))/$,
            $/create table audit.resource_AUD (disc_type varchar(31) not null, resource_id int4 not null, 
REV int4 not null, revtype int2, author varchar(255), description int8, name int8, 
resourceType varchar(255), visibility varchar(255), category_id int8, image_id int8, uri bytea, 
file_id int8, primary key (resource_id, REV))/$,
            $/create table audit.resource_tags_AUD (REV int4 not null, resource_id int4 not null, tag_id int8 not null, 
revtype int2, primary key (REV, resource_id, tag_id))/$,
            $/alter table app.Company add constraint UK_g73lr2xx83k31oh88aggqdmn6 unique (programmaticIdentifier)/$,
            $/create index membership_user_idx on app.Membership (user_id)/$,
            $/create index membership_profile_idx on app.Membership (profile_id)/$,
            $/create index membership_membershiptype_idx on app.Membership (membershiptype_id)/$,
            $/alter table app.MembershipOperation add constraint UK_6ixxu0985dvx1rj7kl09ebg3f unique (programmaticIdentifier)/$,
            $/alter table app.MembershipType add 
constraint profileType_programmaticIdentifier unique (profiletype_id, programmaticIdentifier)/$,
            $/create index profile_disc_idx on app.Profile (disc_type)/$,
            $/create index profile_profletype_idx on app.Profile (profiletype_id)/$,
            $/alter table app.ProfileType add constraint UK_t3j4l3s018c8tjak4bhg5pw0s unique (programmaticIdentifier)/$,
            $/create index schedule_disc_idx on app.Schedule (disc_type)/$,
            $/alter table app.User add constraint UK_ehd3c2thh07pprbxg4ueb44oa unique (principal_id)/$,
            $/create index userposition_user_idx on app.UserPosition (user_id)/$,
            $/alter table app.repositoryItemRelation add constraint repoItem_repo unique (repository_id, repositoryItem_id)/$,
            $/alter table app.resourceRepositoryItem add constraint UK_4licsjknpmd2s7wmmi8a3int8 unique (resource_id)/$,
            $/alter table app.Client add constraint FK_qv107jiv52srlkhy6bx8nmtsr foreign key (logo_id) 
references FileSystemEntity/$,
            $/alter table app.Client add constraint FK_r5ofyc14ybqss89cxr9rkbw0e foreign key (location_id) 
references app.Location/$,
            $/alter table app.Client add constraint FK_t2d5rjf0dgvatnm1krq4ih22l foreign key (profile_id) 
references app.Profile/$,
            $/alter table app.Client_Location add constraint FK_blp7vq6tnna1j43ubk3duxjyo foreign key (location_id) 
references app.Location/$,
            $/alter table app.Client_Location add constraint FK_ecu0h6jotvwie8qbupjgplijl foreign key (profile_id) 
references app.Client/$,
            $/alter table app.Company add constraint FK_6y2hw1nbjx0xrja1eq30m774f foreign key (emaillogo_id) 
references FileSystemEntity/$,
            $/alter table app.Company add constraint FK_qw86iircl8udyv6jpqv5cjwgj foreign key (hostname_id) 
references hostname/$,
            $/alter table app.Company add constraint FK_8ck9kejcvvdicctpmm92mx3b foreign key (image_id) 
references FileSystemEntity/$,
            $/alter table app.Company add constraint FK_jpeoyr69lxwe5djn45frgowco foreign key (location_id) 
references app.Location/$,
            $/alter table app.Company add constraint FK_eufssw03wsavna911p1tta1nr foreign key (profileTerms_id) 
references app.profileterms/$,
            $/alter table app.Company add constraint FK_jw0ahvuv33ff66w3bhd38h2i1 foreign key (resourceCategories_id) 
references LabelDomain/$,
            $/alter table app.Company add constraint FK_c7umjfnlbl81vcqewx79gs4f4 foreign key (resourceTags_id) 
references LabelDomain/$,
            $/alter table app.Company add constraint FK_hv207yvqnwalumvuaojdop67 foreign key (profile_id) 
references app.Profile/$,
            $/alter table app.Company_Location add constraint FK_m4fr5dsrcok92sntixvux0774 foreign key (location_id) 
references app.Location/$,
            $/alter table app.Company_Location add constraint FK_8pvkvuay759gt30at535npxjw foreign key (profile_id) 
references app.Company/$,
            $/alter table app.Location add constraint FK_7nj4el8jy1r97mo2u13gw30ad foreign key (address_id) 
references address/$,
            $/alter table app.Location add constraint FK_ldn6lb7q1hcr3k7ndklnt6w1q foreign key (emailaddress_id) 
references emailaddress/$,
            $/alter table app.Location add constraint FK_simddrhfj9an5c7xaih2vdkoy foreign key (phonenumber_id) 
references phonenumber/$,
            $/alter table app.Location add constraint FK_jicplug3n2u8valmsayo7h9hw foreign key (profile_id) 
references app.Profile/$,
            $/alter table app.Membership add constraint FK_dpwp8sdhpyjjtotiq0ewnkig4 foreign key (createuser_id) 
references Role/$,
            $/alter table app.Membership add constraint FK_jt1rpxggwqso9viu2uf6n8knu foreign key (lastmoduser_id) 
references Role/$,
            $/alter table app.Membership add constraint FK_k62lhjv7dcqprswq3shvsj9qp foreign key (membershiptype_id) 
references app.MembershipType/$,
            $/alter table app.Membership add constraint FK_jikragyivq2ygt6amocvkd7lw foreign key (profile_id) 
references app.Profile/$,
            $/alter table app.Membership add constraint FK_1nhguek78yne96xcq456gfahy foreign key (user_id) 
references app.User/$,
            $/alter table app.MembershipType add constraint FK_ro9a7o3ol9kc3ei5o9amq8m0m foreign key (createuser_id) 
references Role/$,
            $/alter table app.MembershipType add constraint FK_h5ltnsh66d2kv1hjkhad7bfsp foreign key (lastmoduser_id) 
references Role/$,
            $/alter table app.MembershipType add constraint FK_6vf2w3moouao80esf5flhms1 foreign key (profiletype_id) 
references app.ProfileType/$,
            $/alter table app.Note add constraint FK_ke94el4i1mot6pii3qe6tdmf4 foreign key (createuser_id) 
references Role/$,
            $/alter table app.Note add constraint FK_6n12ahk93q6lislpl8wm13550 foreign key (lastmoduser_id) 
references Role/$,
            $/alter table app.Profile add constraint FK_120gu3pb1sjdwin2bcpw2357s foreign key (createuser_id) 
references Role/$,
            $/alter table app.Profile add constraint FK_o9gwbsk398o4qkcrh37wkl661 foreign key (lastmoduser_id) 
references Role/$,
            $/alter table app.Profile add constraint FK_1phcwx0tcaghvjlltm9vdh882 foreign key (parent) 
references app.Profile/$,
            $/alter table app.Profile add constraint FK_86eq83l26mj036rktl8nrlsio foreign key (profiletype_id) 
references app.ProfileType/$,
            $/alter table app.Profile add constraint FK_brfoms31sjtp4bb85xbrnnhc4 foreign key (repository_id) 
references app.repository/$,
            $/alter table app.ProfileDatedRecord add constraint FK_5rxhl481h85j4er38mro9ry97 foreign key (profile_id) 
references app.Profile/$,
            $/alter table app.ProfileType add constraint FK_8jakqkse1778oeyiwy14o2iqo foreign key (createuser_id) 
references Role/$,
            $/alter table app.ProfileType add constraint FK_qryel1i6uk48vc686aork13w4 foreign key (lastmoduser_id) 
references Role/$,
            $/alter table app.ProfileType add constraint FK_oijwrpvd1t5tof88sc0rp971l foreign key (kind_id) 
references Label/$,
            $/alter table app.Schedule add constraint FK_77hcm4h5i2ws4yucjnwru69x3 foreign key (createuser) 
references Role/$,
            $/alter table app.Schedule add constraint FK_tfovuod0gt382elqocm4tqaj7 foreign key (lastmoduser) 
references Role/$,
            $/alter table app.User add constraint FK_i20ejp70fp5113y1v6vbmoxkp foreign key (createuser_id) 
references Role/$,
            $/alter table app.User add constraint FK_e6f4pgiryip91h2tpsgxjjb7t foreign key (lastmoduser_id) 
references Role/$,
            $/alter table app.User add constraint FK_8c3lupj11bodtgy6qhy6dkytu foreign key (image_id) 
references FileSystemEntity/$,
            $/alter table app.User add constraint FK_ehd3c2thh07pprbxg4ueb44oa foreign key (principal_id) 
references Role/$,
            $/alter table app.User add constraint FK_khpkc5u8jh6j1ccvako0ypxt4 foreign key (smsPhone_id) 
references phonenumber/$,
            $/alter table app.UserPosition add constraint FK_6xdgp3cqvausufilo6p6uxg0o foreign key (createuser_id) 
references Role/$,
            $/alter table app.UserPosition add constraint FK_n5671bflh4bysj3xlbjrp8t85 foreign key (lastmoduser_id) 
references Role/$,
            $/alter table app.UserPosition add constraint FK_sieqxw93mvq7uqqdkmd1rwon8 foreign key (user_id) 
references app.User/$,
            $/alter table app.company_user add constraint FK_c5ho50yl88ujw9fvhyicsmb13 foreign key (user_id) 
references app.User/$,
            $/alter table app.company_user add constraint FK_d6syqfgojuse080rmvoo078gh foreign key (profile_id) 
references app.Company/$,
            $/alter table app.ical4jschedule add constraint FK_kv3cgqtbsnq8nww9pao5lvm7v foreign key (ICal4jSchedule_id) 
references app.Schedule/$,
            $/alter table app.membership_operations add constraint FK_dmns569x6n1gti950ktg9uwwe foreign key (membershipOperation_id)
 references app.MembershipOperation/$,
            $/alter table app.membership_operations add constraint FK_n73mw17rewdvopj3v7a0hkn9v foreign key (membership_id) 
references app.Membership/$,
            $/alter table app.membershiptype_operations add constraint FK_ft6wguacgi13qrblyagqedgdx 
foreign key (membershipOperation_id) references app.MembershipOperation/$,
            $/alter table app.membershiptype_operations add constraint FK_g0e9q2294161aiv3l7xtir251 
foreign key (membershiptype_id) references app.MembershipType/$,
            $/alter table app.relativeperiodschedule add constraint FK_dc3167603jugwpp96g2fddvxt 
foreign key (RelativePeriodSchedule_id) references app.Schedule/$,
            $/alter table app.repository add constraint FK_hbh8xhafqcsthsf2jhmse4l47 foreign key (createuser_id) 
references Role/$,
            $/alter table app.repository add constraint FK_mjxadc5ro7s55gmx4acu0iqy foreign key (lastmoduser_id) 
references Role/$,
            $/alter table app.repositoryItem add constraint FK_gvs7k3a6aifosxtv67kkhabdp foreign key (createuser_id) 
references Role/$,
            $/alter table app.repositoryItem add constraint FK_k92q40c06k4kwcdwvycpb8l6v foreign key (lastmoduser_id) 
references Role/$,
            $/alter table app.repositoryItemRelation add constraint FK_a3rqsg61vwxp23v7jw894sl1l foreign key (createuser_id) 
references Role/$,
            $/alter table app.repositoryItemRelation add constraint FK_3owbhsw3pymcohuf8u18vow0q foreign key (lastmoduser_id) 
references Role/$,
            $/alter table app.repositoryItemRelation add constraint FK_b9cc8u66xrjsbfb9xr1ralkwh foreign key (repository_id) 
references app.repository/$,
            $/alter table app.repositoryItemRelation add constraint FK_41b16p1n2aclr7ftcmwlubbin foreign key (repositoryItem_id) 
references app.repositoryItem/$,
            $/alter table app.resource add constraint FK_cb1xdbpgpniw7u8066puksmvn foreign key (createuser_id) 
references Role/$,
            $/alter table app.resource add constraint FK_5xked31t6a9jfgss889pynvo1 foreign key (lastmoduser_id) 
references Role/$,
            $/alter table app.resource add constraint FK_8xgm54fn739t9pn2t7v5imy2x foreign key (category_id) 
references Label/$,
            $/alter table app.resource add constraint FK_fbaoe31va4gkv37akp9jb46rx foreign key (image_id) 
references FileSystemEntity/$,
            $/alter table app.resource add constraint FK_12atl5vumm3ggapey4m0ry6ir foreign key (file_id) 
references FileSystemEntity/$,
            $/alter table app.resourceRepositoryItem add constraint FK_4licsjknpmd2s7wmmi8a3int8 foreign key (resource_id) 
references app.resource/$,
            $/alter table app.resourceRepositoryItem add constraint FK_eaql5gq2fa5ytnb3crj0nad5o foreign key (repositoryItem_id) 
references app.repositoryItem/$,
            $/alter table app.resource_tags add constraint FK_7yldi7n51rjh4fu9xkl96jqqh foreign key (tag_id) 
references Label/$,
            $/alter table app.resource_tags add constraint FK_qxlhk7ik7lc5hgiw3yyodnsvo foreign key (resource_id) 
references app.resource/$,
            $/alter table audit.Client_AUD add constraint FK_h1q7jtv2h7k8r79m2jppq2i0t foreign key (profile_id, REV) 
references audit.Profile_AUD/$,
            $/alter table audit.Client_Location_AUD add constraint FK_mnfbjm1li0e08mbcslo0o29n8 foreign key (REV) 
references audit.REVINFO/$,
            $/alter table audit.Company_AUD add constraint FK_761g67etw834bjydhpdon745j foreign key (profile_id, REV) 
references audit.Profile_AUD/$,
            $/alter table audit.Company_Location_AUD add constraint FK_oww3lvuauuqeqodnlk73fhcga foreign key (REV) 
references audit.REVINFO/$,
            $/alter table audit.Location_AUD add constraint FK_blt1n72ywir2hu1dlrwvwcxqu foreign key (profile_id, REV) 
references audit.Profile_AUD/$,
            $/alter table audit.MembershipType_AUD add constraint FK_j0y2o79gn5fvcmkgto3tglc37 foreign key (REV) 
references audit.REVINFO/$,
            $/alter table audit.Membership_AUD add constraint FK_5w0qxj78mgtkll2t2v5tmh7ru foreign key (REV) 
references audit.REVINFO/$,
            $/alter table audit.Note_AUD add constraint FK_svabcv1pycuqt7bcme481wuvg foreign key (REV) 
references audit.REVINFO/$,
            $/alter table audit.ProfileType_AUD add constraint FK_tyugg30iusi3mbj8tqgiivx foreign key (REV) 
references audit.REVINFO/$,
            $/alter table audit.Profile_AUD add constraint FK_sxglrbe43jmbvthlep203rwqq foreign key (REV) 
references audit.REVINFO/$,
            $/alter table audit.Schedule_AUD add constraint FK_dasyt80y84r7yp28nwmw8k7ev foreign key (REV) 
references audit.REVINFO/$,
            $/alter table audit.UserPosition_AUD add constraint FK_8xbe5s4q57yusgfejgipgu147 foreign key (REV) 
references audit.REVINFO/$,
            $/alter table audit.User_AUD add constraint FK_97pph94d4cb7qah5aygmmll2y foreign key (REV) 
references audit.REVINFO/$,
            $/alter table audit.company_user_AUD add constraint FK_g0wcf8u9vwcecs1kiua8a8b4b foreign key (REV) 
references audit.REVINFO/$,
            $/alter table audit.ical4jschedule_AUD add constraint FK_jeh5q2v29ikcuco5altja5sov foreign key (ICal4jSchedule_id, REV)
 references audit.Schedule_AUD/$,
            $/alter table audit.membership_operations_AUD add constraint FK_1h7caud9oufwaunvqgkbvsdqp foreign key (REV)
 references audit.REVINFO/$,
            $/alter table audit.membershiptype_operations_AUD add constraint FK_on7ockr4feg5jbhof7utdvvy7 foreign key (REV)
 references audit.REVINFO/$,
            $/alter table audit.profileterms_AUD add constraint FK_jee7gcucver5x6df9dgj4uo9g foreign key (REV)
 references audit.REVINFO/$,
            $/alter table audit.relativeperiodschedule_AUD add constraint FK_78wtb0hshvemw4px7mn2t8aip 
foreign key (RelativePeriodSchedule_id, REV) references audit.Schedule_AUD/$,
            $/alter table audit.repositoryItemRelation_AUD add constraint FK_hl6cxng21u1j2oolj0q04imiq foreign key (REV) 
references audit.REVINFO/$,
            $/alter table audit.repositoryItem_AUD add constraint FK_m6ay2qlbxt6oq2u6uepagkle9 foreign key (REV) 
references audit.REVINFO/$,
            $/alter table audit.repository_AUD add constraint FK_p4468lkc0xlbj756l8sbhj44w foreign key (REV) 
references audit.REVINFO/$,
            $/alter table audit.resourceRepositoryItem_AUD add constraint FK_3w87mbmq4xknd8ei7b5vwj24s 
foreign key (repositoryItem_id, REV) references audit.repositoryItem_AUD/$,
            $/alter table audit.resource_AUD add constraint FK_d8kbtwiryi5847hfaa1a5nrr4 foreign key (REV) 
references audit.REVINFO/$,
            $/alter table audit.resource_tags_AUD add constraint FK_dfpenf8y06nw7iqagi9w9hjxt foreign key (REV) 
references audit.REVINFO/$,
            $/create sequence app.membershipOperation_id_seq/$,
            $/create sequence app.membership_id_seq/$,
            $/create sequence app.membershiptype_id_seq/$,
            $/create sequence app.note_id_seq/$,
            $/create sequence app.profileDatedRecord_id_seq/$,
            $/create sequence app.profile_id_seq/$,
            $/create sequence app.profileterms_id_seq/$,
            $/create sequence app.profiletype_id_seq/$,
            $/create sequence app.repositoryItemRelation_id_seq/$,
            $/create sequence app.repositoryItem_id_seq/$,
            $/create sequence app.repository_id_seq/$,
            $/create sequence app.resource_id_seq/$,
            $/create sequence app.schedule_id_seq start 1 increment 5/$,
            $/create sequence app.user_id_seq/$,
            $/create sequence app.user_position_id_seq/$,
            $/create sequence hibernate_sequence/$,
        ]
        return new SQLDataConversion(IDENTIFIER, 'initial data conversion', 201612301907, false, null, ddl, null, null)
    }
}
