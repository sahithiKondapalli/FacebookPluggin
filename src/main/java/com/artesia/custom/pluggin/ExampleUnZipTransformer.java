package com.artesia.custom.pluggin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.artesia.common.exception.BaseTeamsException;
import com.artesia.export.ExportList;
import com.artesia.server.common.plugin.ServerPluginResources;
import com.artesia.server.transformer.ExportJobTransformationResult;
import com.artesia.server.transformer.ExportJobTransformer;
import com.artesia.server.transformer.TransformerAssetWrapper;
import com.artesia.transformer.TransformerArgument;
import com.artesia.transformer.TransformerInfo;

public class ExampleUnZipTransformer implements ExportJobTransformer
{
    private final static Logger log = Logger
            .getLogger(ExampleUnZipTransformer.class.getName());

    @Override
    public void initialize(TransformerInfo arg0, Map<String, String> arg1)
    {
    	//Nothing do.
    }

    @Override
    public ExportJobTransformationResult transformJobContent(List<File> transformedFiles,
            List<TransformerAssetWrapper> assetList, File exportXmlFile, File workingDir,
            List<TransformerArgument> argList, ExportList exportList,
            ServerPluginResources resources) throws BaseTeamsException
    {
        log.info("Inside custom transformJobContent");
        ExportJobTransformationResult exportJobTransformationResult = new ExportJobTransformationResult(
                true);
        List<File> videoAssets = new ArrayList<>();
        for (TransformerAssetWrapper asset : assetList)
        {
            if ("VIDEO".equals(asset.getAsset().getContentType()))
            {
                videoAssets.add(asset.getMasterFile());
            }
            else
            {
                asset.getMasterFile().delete();
            }
        }

        /* List<String> videoAssetList = assetList.stream()
                .filter(asset -> "VIDEO".equals(asset.getAsset().getContentType()))
                .map(asset -> asset.getAsset().getAssetId().asString())
                .collect(Collectors.toList());

        List<ExportElement> elementList = exportList.getElementList();
        Iterator<ExportElement> elementIterator = elementList.iterator();
        while (elementIterator.hasNext())
        {
            ExportElement type = (ExportElement) elementIterator.next();
            if (!videoAssetList.contains(type.getElementId().asString()))
            {
                elementIterator.remove();
            }
        }*/

        if (videoAssets.isEmpty())
        {
            exportJobTransformationResult
                    .setTransformationErrorMessageKey("Invalid Assets Selected");
            exportJobTransformationResult.setTransformationSuccessful(false);
        }
        
        else if (!(videoAssets.size() == transformedFiles.size()))
        {
            exportJobTransformationResult.setTransformationErrorMessageKey(
                    "Export of " + videoAssets.size() + " asset initiated and "
                            + (transformedFiles.size() - videoAssets.size()) + " failed");
            exportJobTransformationResult.setTransformationSuccessful(true);
        }
        else
        {
            exportJobTransformationResult.setTransformationSuccessful(true);
        }
        exportJobTransformationResult.setTransformedFiles(videoAssets);
        return exportJobTransformationResult;
    }

}
