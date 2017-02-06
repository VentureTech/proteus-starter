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

package com.proteusframework.build

import javax.xml.parsers.DocumentBuilderFactory

/**
 * Class for defining methods for downloading files from Artifactory
 * @author Alan Holt (aholt@venturetech.net)
 * @since 2/3/17
 */
class ArtifactoryDownloader {
    static class ArtifactoryCredentials {
        final String username
        final String password
        final String encoded

        /**
         * Constructs a new ArtifactoryCredentials object.
         * @param username the username for Artifactory
         * @param password the password for Artifactory
         */
        ArtifactoryCredentials(String username, String password) {
            this.username = username
            this.password = password

            def encoding = Base64.encoder.encodeToString("${this.username}:${this.password}".bytes)
            this.encoded = "Basic ${encoding}"
        }
    }

    /**
     * Gets {@link ArtifactoryCredentials} from your system's gradle.properties file.
     * @return an ArtifactoryCredentials object.
     * @throws IllegalStateException if artifactory credentials are not present within the file, or the file is not present.
     */
    ArtifactoryCredentials getArtifactoryCredentials() throws IllegalStateException {
        def home = System.getenv()['HOME']
        def gradleProperties = new File("${home}/.gradle/gradle.properties")
        if(!gradleProperties.exists())
            throw new IllegalStateException("File: ${gradleProperties.path} does not exist.  Unable to download from Artifactory.")
        if(!gradleProperties.canRead())
            throw new IllegalStateException("Unable to read from file: ${gradleProperties.path}. "
                + "Unable to download from Artifactory")
        def properties = new Properties()
        gradleProperties.withInputStream { properties.load(it) }
        def username = properties["repo_venturetech_username"] as String
        def password = properties["repo_venturetech_password"] as String
        if(!username || !password)
            throw new IllegalStateException("Username and Password must be included in ${gradleProperties.path} file with key "
                + "names: 'repo_venturetech_username' and 'repo_venturetech_password'")
        return new ArtifactoryCredentials(username, password)
    }

    /**
     * Downloads the file for the given URL from Artifactory
     * @param credentials artifactory credentials
     * @param url the url to the file
     * @return the local file
     */
    File downloadFile(ArtifactoryCredentials credentials, URL url, String extension = '.zip') {
        def resolvedUrl = resolveURL(credentials, url)
        def file = File.createTempFile('artifactory-download', extension)
        file.deleteOnExit()
        println("Downloading ${resolvedUrl.toExternalForm()}")
        def connection = resolvedUrl.openConnection()
        setAuthority(connection, credentials)
        connection.inputStream.withCloseable { ins ->
            file.withOutputStream { ous ->
                copyStream(ins, ous)
        }   }
        return file
    }

    private setAuthority(URLConnection connection, ArtifactoryCredentials credentials) {
        connection.setRequestProperty("Authorization", credentials.encoded)
    }

    private int copyStream(InputStream source, OutputStream dest) throws IOException {
        byte[] buf = new byte[4096]
        int count, total = 0
        while ((count = source.read(buf)) != -1)
        {
            if (count > 0)
            {
                total += count
                dest.write(buf, 0, count)
            }
        }
        return total
    }

    /**
     * Resolves the given URL, replacing instances of 'LATEST' with a valid version number from Artifactory.
     * Reads maven-metadata.xml from Artifactory to do so.
     * @param credentials artifactory credentials
     * @param url the url to resolve
     * @return the resolved url
     */
    URL resolveURL(ArtifactoryCredentials credentials, URL url) {
        if(!url) return url
        def urlString = url.toExternalForm()
        if(urlString.contains("\${LATEST}")) {
            def metaDataURL = new URL(urlString.substring(0, urlString.indexOf("\${LATEST}")) + "maven-metadata.xml")
            def connection = metaDataURL.openConnection()
            setAuthority(connection, credentials)
            def version = ''
            connection.inputStream.withCloseable {
                def dbf = DocumentBuilderFactory.newInstance()
                def db = dbf.newDocumentBuilder()
                def doc = db.parse(it)
                def childNodes = doc.documentElement.childNodes
                version = getVersion(childNodes)
            }
            if(version != null && version != '') {
                return new URL(urlString.replaceAll("\$\\{LATEST}", version))
            }
        }
        return url
    }

    private String getVersion(org.w3c.dom.NodeList nodes) {
        def version = null
        for(int i = 0; i < nodes.length; i++) {
            def node = nodes.item(i)
            if(node.nodeName == "latest"){
                version = node.textContent
                break
            }
            version = getVersion(node.childNodes)
            if(version != null) break
        }
        return version
    }
}
