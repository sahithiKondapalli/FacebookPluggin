package com.artesia.custom.pluggin;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.logging.Logger;

import com.artesia.common.exception.BaseTeamsException;
import com.artesia.common.utils.FilenameConflictUtils;
import com.artesia.entity.TeamsIdentifier;
import com.artesia.manageddirectory.ManagedDirectory;
import com.artesia.manageddirectory.services.ManagedDirectoryServices;
import com.artesia.security.SecuritySession;
import com.artesia.server.common.plugin.ServerPluginResources;
import com.artesia.server.transformer.BaseTransformer;
import com.artesia.server.transformer.ExportJobTransformationResult;
import com.artesia.server.transformer.ManagedDirectoryTranformer;
import com.artesia.server.transformer.ManagedDirectoryTransformerResult;
import com.artesia.transformer.TransformerInfo;
import com.artesia.transformer.TransformerInstance;
import com.artesia.transformer.TransformerInstanceValue;
import com.restfb.BinaryAttachment;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.FacebookType;
import com.restfb.types.User;

public class DelivertoFaceBookExportJobTransformer extends BaseTransformer
        implements ManagedDirectoryTranformer
{
    private final static Logger log = Logger
            .getLogger(DelivertoFaceBookExportJobTransformer.class.getName());
    public ExportJobTransformationResult transformResult = new ExportJobTransformationResult(
            true);
    private SecuritySession securitySession;
    private ManagedDirectoryTransformerResult managedDirectoryTransformerResult = new ManagedDirectoryTransformerResult();
    private ManagedDirectoryServices managedDirectoryServices;

    @Override
    public void initialize(TransformerInfo info,
            Map<String, String> configurationAttributesMap)
    {  	
    	
    }

    @Override
    public ManagedDirectoryTransformerResult executeTransformer(
            TeamsIdentifier managedDirectoryId, TransformerInstance transformerInstance,
            ServerPluginResources serverPluginResources) throws BaseTeamsException
    {
        log.info("Inside executeTransformer");
        this.securitySession = serverPluginResources.getSecuritySession();
        for (TransformerInstanceValue value : transformerInstance.getAttributeValues()) {
            log.info(Integer.valueOf(value.getArgumentNumber()) + " instance map values " + value.getValue());
          }
        // Retrive Information About ManagedDirectory
        ManagedDirectory managedDirectory = getManagedDirectory(managedDirectoryId,
                securitySession);
        uploadToFacebook(managedDirectory, transformerInstance);
        log.info("completed uploading to FaceBook");
        return this.managedDirectoryTransformerResult;
    }

    private void uploadToFacebook(ManagedDirectory managedDirectory, TransformerInstance transformerInstance)
            throws BaseTeamsException
    {

        File file = new File(FilenameConflictUtils.verifyFilePathForDataTraversal(
                managedDirectory.getFullyQualifiedPath()));
        log.info("ManagedDirectoryPath" + managedDirectory.getFullyQualifiedPath());
        String accessToken = "EAAUMDZCr1pE8BAGfhnNVuBQaJe3tZBUdm9l3TLcbM3cAsJ8lZCC0liAsvG27f3tTjOkHAd0nEIOtRYxPuXeRerKXBVxFf7WKVxnwjWB8ZBANaMNRAS6oiEPY9bQX8e62kwbDu5dMBOSB1kDe5Ol8nm8V5FlcEvMZD";

        // Initilization of facebook client with accesstoken
        FacebookClient fbClient = new DefaultFacebookClient(accessToken,
                Version.VERSION_2_8);

        // Facebook UserProfile Object
        User me = fbClient.fetchObject("me", User.class);
        log.info("Facebook profile Name" + me.getName());

        if ((file.isDirectory()) && (file.isAbsolute()))
        {
            File[] unzippedFiles = file.listFiles();
            for (File subFile : unzippedFiles)
            {
                if (!subFile.getName().endsWith("xml"))
                {
                    byte[] bFile = new byte[(int) subFile.length()];
                    try (
                            // convert file into array of bytes
                            FileInputStream fileInputStream = new FileInputStream(
                                    subFile))
                    {
                        fileInputStream.read(bFile);
                        FacebookType response = fbClient.publish("me/videos",
                                FacebookType.class,
                                BinaryAttachment.with(subFile.getName(), bFile),
                                Parameter.with("message", "My video"));
                        log.info("Facebook response" + response.getId());
                    }
                    catch (Exception e)
                    {
                        log.info("exception occured" + e.getMessage());
                        this.managedDirectoryTransformerResult
                                .setTransformationSuccessful(false);
                    }
                }
            }
            this.managedDirectoryTransformerResult.setTransformationSuccessful(true);
        }
    

    }

    private ManagedDirectory getManagedDirectory(TeamsIdentifier managedDirectoryId,
            SecuritySession securitySession) throws BaseTeamsException
    {
        return getManagedDirectoryServices().retrieveManagedDirectory(managedDirectoryId,
                securitySession);
    }

    private ManagedDirectoryServices getManagedDirectoryServices()
    {
        if (this.managedDirectoryServices == null)
        {
            this.managedDirectoryServices = ManagedDirectoryServices.getInstance();
        }
        return this.managedDirectoryServices;
    }

}
