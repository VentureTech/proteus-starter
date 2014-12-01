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

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.Nullable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Date;

import net.proteusframework.cms.CmsSite;
import net.proteusframework.users.audit.TimeAuditable;
import net.proteusframework.users.model.Name;

/**
 * Professor entity
 *
 * @author Yinge Huang (yhuang@venturetechasia.net)
 * @since 11/11/14 2:16 AM
 */
@Entity
@Table(name="professor")
public class Professor implements TimeAuditable
{
	/** Sequence name. */
	private final static String SEQ = "professor_seq";
	/** Identifier. */
	private long _id;
	/** Name - we'll use some of the properties of this class. */
	private Name _name = new Name();
	/** If the slug is “jsmith”, the faculty details URL will be /details/jsmith */
	private String _slug="";
	/** rank*/
	private Rank _rank;
	/** Date the professor joined the university*/
	private Date _startDate;
	/** Research area specialty*/
	private String _researchArea="";
	/** Boolean flag if the professor is on sabbatical*/
	private boolean _isSabbatical;
	/**the time of user mode professor information*/
	private Date _lastModTime=new Date();
	/**the time of user create professor entity*/
	private Date _createTime=_lastModTime;
	/** Site. */
	private CmsSite _site;

	/**
	 * constructor method
	 */
	public Professor()
	{
		super();
	}

	/**
	 * Copy constructor.
	 *
	 * @param toCopy profile to copy.
	 */
	public Professor(Professor toCopy)
	{
		super();
		setName(new Name(toCopy.getName()));
		setSlug(toCopy.getSlug());
		setRank(toCopy.getRank());
		setStartDate(toCopy.getStartDate());
		setResearchArea(toCopy.getResearchArea());
		setSabbatical(toCopy.isSabbatical());
		setSite(toCopy.getSite());
	}


	/**
	 * Get the identifier.
	 *
	 * @return the identifier.
	 */
	@Id
	@NotNull
	@GeneratedValue(strategy = GenerationType.AUTO, generator = SEQ)
	@SequenceGenerator(name = SEQ, sequenceName = SEQ)
	@Column(name = "professor_id")
	public Long getId()
	{
		return _id;
	}

	/**
	 * Set the identifier.
	 *
	 * @param id the identifier.
	 */
	public void setId(Long id)
	{
		_id = id;
	}



	/**
	 * Get the name.
	 *
	 * @return the name.
	 */
	@ManyToOne(cascade = {CascadeType.ALL})
	@NotNull
	public Name getName()
	{
		return _name;
	}

	/**
	 * Set the name.
	 *
	 * @param name the name.
	 */
	public void setName(Name name)
	{
		if (name == null)
			_name = new Name();
		else
			_name = name;
	}

	/**
	 * Get the slug
	 * @return the slug
	 */
	public String getSlug()
	{
		return _slug;
	}

	/**
	 * Set the slug
	 * @param slug the slug
	 */
	public void setSlug(@Nullable String slug)
	{
		if(slug==null)
		{
			_slug="";
		}
		else
			_slug = slug;
	}

	/**
	 * Get the rank
	 * @return the rank
	 */
	@Enumerated(EnumType.STRING)
	public Rank getRank()
	{
		return _rank;
	}

	/**
	 * Set the rank
	 * @param rank the rank
	 */
	public void setRank(Rank rank)
	{
		_rank = rank;
	}

	/**
	 * Get the date of professor join the school
	 * @return
	 */
	public Date getStartDate()
	{
		return _startDate;
	}

	/**
	 * Set the joinDate
	 * @param startDate the joinDate
	 */
	public void setStartDate(Date startDate)
	{
		_startDate = startDate;
	}

	/**
	 * Get the researchArea
	 * @return the researchArea
	 */
	public String getResearchArea()
	{
		return _researchArea;
	}

	/**
	 * Set the researchArea
	 * @param researchArea the researchArea
	 */
	public void setResearchArea(String researchArea)
	{
		_researchArea = researchArea;
	}

	/**
	 * If the professor is sabbatical
	 * @return true or false
	 */
	public boolean isSabbatical()
	{
		return _isSabbatical;
	}

	/**
	 * Set true or false
	 * @param isSabbatical if the professor is sabbatical,return true
	 */
	public void setSabbatical(boolean isSabbatical)
	{
		_isSabbatical = isSabbatical;
	}

	/**
	 * Get the createTime
	 * @return the createTime
	 */
	@Override
	public Date getCreateTime()
	{
		return _createTime;
	}

	/**
	 * Get the lastModTime
	 * @return the lastModTime
	 */
	@Override
	public Date getLastModTime()
	{
		return _lastModTime;
	}

	/**
	 * Set the lastModTime
	 * @param lastModTime the lastModTime
	 */
	@Override
	public void setLastModTime(Date lastModTime)
	{
		Preconditions.checkNotNull(lastModTime);
		_lastModTime=lastModTime;
	}

	/**
	 * Set the createTime
	 * @param createTime the createTime
	 */
	@Override
	public void setCreateTime(Date createTime)
	{
		_createTime=createTime;
	}

	/**
	 * Get the site.
	 *
	 * @return the site.
	 */
	@ManyToOne
	// NOTE: Some CMS entities are mapped using XML. Hibernate gives them a goofy column name like "site_site_id".
	//  we want to make the name make sense, so we'll override the default name in this case.
	@JoinColumn(name = "site_id")
	public CmsSite getSite()
	{
		return _site;
	}

	/**
	 * Set the site that the professor profile is associated with.
	 *
	 * @param site the site.
	 */
	public void setSite(CmsSite site)
	{
		_site = site;
	}

}

