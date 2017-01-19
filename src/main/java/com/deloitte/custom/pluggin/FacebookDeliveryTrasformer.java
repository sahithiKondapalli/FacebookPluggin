package com.deloitte.custom.pluggin;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.logging.Logger;

import com.artesia.common.exception.BaseTeamsException;
import com.artesia.common.utils.FilenameConflictUtils;
import com.artesia.entity.TeamsIdentifier;
import com.artesia.manageddirectory.ManagedDirectory;
import com.artesia.security.SecuritySession;
import com.artesia.server.common.plugin.ServerPluginResources;
import com.artesia.server.common.task.TaskRequest;
import com.artesia.server.common.task.TaskResult;
import com.artesia.server.manageddirectory.task.RetrieveManagedDirectoryTask;
import com.artesia.server.storage.StorageContext;
import com.artesia.server.transformer.ManagedDirectoryTranformer;
import com.artesia.server.transformer.ManagedDirectoryTransformerResult;
import com.artesia.server.transformer.Transformer;
import com.artesia.transformer.TransformerInfo;
import com.artesia.transformer.TransformerInstance;
import com.restfb.BinaryAttachment;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.FacebookType;
import com.restfb.types.User;

/**
 * @author skondapalli
 *
 */
public class FacebookDeliveryTrasformer implements ManagedDirectoryTranformer, Transformer {

	private final static Logger log = Logger.getLogger(FacebookDeliveryTrasformer.class.getName());
	private SecuritySession securitySession = null;
	 ManagedDirectoryTransformerResult managedDirectoryTransformerResult = new ManagedDirectoryTransformerResult();

	@Override
	public ManagedDirectoryTransformerResult executeTransformer(TeamsIdentifier managedDirectoryId,
			TransformerInstance transformerInstance, ServerPluginResources serverPluginResources)
			throws BaseTeamsException {
		this.securitySession = serverPluginResources.getSecuritySession();
		ManagedDirectory managedDirectory = getManagedDirectory(managedDirectoryId, this.securitySession,
				serverPluginResources.getStorageContext());
		uploadFacebook(managedDirectory);
		
		return this.managedDirectoryTransformerResult;
	}

	/**
	 * @param managedDirectory
	 * @throws BaseTeamsException
	 * uploadFacebook to upload the shared asset to the facebook
	 */
	public void uploadFacebook(ManagedDirectory managedDirectory) throws BaseTeamsException {
		File file = new File(
				FilenameConflictUtils.verifyFilePathForDataTraversal(managedDirectory.getFullyQualifiedPath()));
		log.info("ManagedDirectoryPath" + managedDirectory.getFullyQualifiedPath());
		String accessToken = "EAAUMDZCr1pE8BAGfhnNVuBQaJe3tZBUdm9l3TLcbM3cAsJ8lZCC0liAsvG27f3tTjOkHAd0nEIOtRYxPuXeRerKXBVxFf7WKVxnwjWB8ZBANaMNRAS6oiEPY9bQX8e62kwbDu5dMBOSB1kDe5Ol8nm8V5FlcEvMZD";
		
		//Initilization of facebook client with accesstoken
		FacebookClient fbClient = new DefaultFacebookClient(accessToken, Version.VERSION_2_8);
		
		//Facebook UserProfile Object
		User me = fbClient.fetchObject("me", User.class);
		log.info("Facebook profile email" + me.getEmail());
		
		if ((file.isDirectory()) && (file.isAbsolute())) {
			File[] filesContained = file.listFiles();

			for (File subFile : filesContained) {

				log.info("file name" + subFile.getName());
				FileInputStream fileInputStream = null;
				byte[] bFile = new byte[(int) subFile.length()];
				try {
					// convert file into array of bytes
					fileInputStream = new FileInputStream(subFile);
					fileInputStream.read(bFile);
					fileInputStream.close();
					FacebookType response = fbClient.publish("me/videos", FacebookType.class,
							BinaryAttachment.with(subFile.getName(), bFile), Parameter.with("message", "My video"));
					log.info("Facebook response"+response.getId());
				} catch (Exception e) {
					log.info("exception occured" + e.getMessage());
				}
			}
			 this.managedDirectoryTransformerResult.setTransformationSuccessful(true);
		}
	}

	@Override
	public void initialize(TransformerInfo arg0, Map<String, String> arg1) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param managedDirectoryId
	 * @param securitySession
	 * @param storageContext
	 * @return
	 * @throws BaseTeamsException
	 */
	protected ManagedDirectory getManagedDirectory(TeamsIdentifier managedDirectoryId, SecuritySession securitySession,
			StorageContext storageContext) throws BaseTeamsException {
		RetrieveManagedDirectoryTask retrieveManagedDirectory = new RetrieveManagedDirectoryTask(managedDirectoryId);
		TaskRequest request = new TaskRequest(storageContext);
		TaskResult<ManagedDirectory> taskResult = retrieveManagedDirectory.execute(request, securitySession);
		ManagedDirectory managedDirectory = (ManagedDirectory) taskResult.getResultObject();

		return managedDirectory;
	}

}
