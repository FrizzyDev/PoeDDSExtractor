package com.github.frizzy.PoeDDSExtractor.Testing;

import com.github.frizzy.PoeDDSExtractor.DDSFile;
import com.github.frizzy.PoeDDSExtractor.GGPKUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Showcases how you could build a wanted textures list dependent on the wanted .dds files.
 * <br>
 * It should be noted that you have to provide the paths to both the .dds files and textures in
 * each .dds file. Paths for both can be located and copied using VisualGPPK.
 *
 * @author Frizzy
 */
public class WantedTextureBuilder {

    private Path txtFile;

    /**
     * The list does not necessarily have to be the converted files. A list of all the .dds files
     * can be used as well, as each file is just used to locate the path.txt file that was created when
     * originally extracting the .dds files.
     */
    public WantedTextureBuilder ( Path uiTxtFile ) {
        this.txtFile = uiTxtFile;
    }

    /**
     * Builds a map, with the keys being either a .dds or .png file reference, and the values being a List
     * of paths pointing to the textures stored in each file.
     */
    public List < DDSFile > buildWantedTextures ( List < DDSFile > ddsFiles ) throws IOException {
        for ( DDSFile dFile : ddsFiles ) {
                String path = dFile.getDdsPath();

                switch ( path ) {
                    case "art/textures/interface/2d/2dart/uiimages/common/4k/1.dds" -> {
                        dFile.setUnextractedTextures( GGPKUtils.getSpecificTexturesFor( txtFile, path, getWanted4k1DDSTextures() ) );
                    }
                    case "art/textures/interface/2d/2dart/uiimages/common/4k/2.dds" -> {
                        dFile.setUnextractedTextures( GGPKUtils.getSpecificTexturesFor( txtFile, path, getWanted4k2DDSTextures() ) );
                    }
                    case "art/textures/interface/2d/2dart/uiimages/common/4k/3.dds" -> {
                        dFile.setUnextractedTextures( GGPKUtils.getSpecificTexturesFor( txtFile, path, getWanted4k3DDSTextures() ) );
                    }
                    case "art/textures/interface/2d/2dart/uiimages/ingame/4k/6.dds" -> {
                        dFile.setUnextractedTextures( GGPKUtils.getSpecificTexturesFor( txtFile, path, getWanted4k6DDSTextures() ) );
                    }
                    case "art/textures/interface/2d/2dart/uiimages/ingame/4k/11.dds"  -> {
                        dFile.setUnextractedTextures( GGPKUtils.getSpecificTexturesFor( txtFile, path, getWanted4k11DDSTextures() ) );
                    }
                    case "art/textures/interface/2d/2dart/uiimages/ingame/1.dds" -> {
                        dFile.setUnextractedTextures( GGPKUtils.getSpecificTexturesFor( txtFile, path, getWanted1DDSTextures() ) );
                    }
                    case "art/textures/interface/2d/2dart/uiimages/ingame/2.dds" -> {
                        dFile.setUnextractedTextures( GGPKUtils.getSpecificTexturesFor( txtFile, path, getWanted2DDSTextures() ) );
                    }
                    case "art/textures/interface/2d/2dart/uiimages/ingame/3.dds" -> {
                        dFile.setUnextractedTextures( GGPKUtils.getSpecificTexturesFor( txtFile, path, getWanted3DDSTextures() ) );
                    }
                    case "art/textures/interface/2d/2dart/uiimages/ingame/4.dds" -> {
                        dFile.setUnextractedTextures( GGPKUtils.getSpecificTexturesFor( txtFile, path, getWanted4DDSTextures() ) );
                    }
                    case "art/textures/interface/2d/2dart/uiimages/ingame/5.dds" -> {
                        dFile.setUnextractedTextures( GGPKUtils.getSpecificTexturesFor( txtFile, path, getWanted5DDSTextures() ) );
                    }
                    case "art/textures/interface/2d/2dart/uiimages/ingame/7.dds" -> {
                        dFile.setUnextractedTextures( GGPKUtils.getSpecificTexturesFor( txtFile, path, getWanted7DDSTextures() ) );
                    }
                    case "art/textures/interface/2d/2dart/uiimages/ingame/10.dds" -> {
                        dFile.setUnextractedTextures( GGPKUtils.getSpecificTexturesFor( txtFile, path, getWanted10DDSTextures() ) );
                    }
                    case "art/textures/interface/2d/2dart/uiimages/ingame/13.dds" -> {
                        dFile.setUnextractedTextures( GGPKUtils.getSpecificTexturesFor( txtFile, path, getWanted13DDSTextures() ) );;
                    }
                    case "art/textures/interface/2d/2dart/uiimages/ingame/15.dds" -> {
                        dFile.setUnextractedTextures( GGPKUtils.getSpecificTexturesFor( txtFile, path, getWanted15DDSTextures() ) );
                    }
                    case "art/textures/interface/2d/2dart/uiimages/ingame/16.dds" -> {
                        dFile.setUnextractedTextures( GGPKUtils.getSpecificTexturesFor( txtFile, path, getWanted16DDSTextures() ) );
                    }
                    case "art/textures/interface/2d/2dart/uiimages/ingame/17.dds" -> {
                        dFile.setUnextractedTextures( GGPKUtils.getSpecificTexturesFor( txtFile, path, getWanted17DDSTextures() ) );
                    }
                    case "art/textures/interface/2d/2dart/uiimages/ingame/marketplace/1.dds" -> {
                        dFile.setUnextractedTextures( GGPKUtils.getSpecificTexturesFor( txtFile, path, getWantedM1DDSTextures() ) );
                    }
                    case "art/textures/interface/2d/2dart/uiimages/ingame/marketplace/2.dds" -> {
                        dFile.setUnextractedTextures( GGPKUtils.getSpecificTexturesFor( txtFile, path, getWantedM2DDSTextures() ) );
                    }

            }
        }

        return ddsFiles;
    }

    public List < String > getWanted4k1DDSTextures ( ) {
        List < String > textures = new ArrayList <>(  );

        textures.add( "Art/2DArt/UIImages/Common/4K/LoadingScreenGear" );
        textures.add( "Art/2DArt/UIImages/Common/4K/ButtonTickPressed" );
        textures.add( "Art/2DArt/UIImages/Common/4K/ButtonTickNormal" );
        textures.add( "Art/2DArt/UIImages/Common/4K/ButtonTickHover" );
        textures.add( "Art/2DArt/UIImages/Common/4K/CheckBoxUnchecked" );
        textures.add( "Art/2DArt/UIImages/Common/4K/WindowTitlebarRight" );
        textures.add( "Art/2DArt/UIImages/Common/4K/WindowTitlebarLeft" );

        return textures;
    }

    public List < String > getWanted4k2DDSTextures ( ) {
        List < String > textures = new ArrayList <>(  );

        textures.add( "Art/2DArt/UIImages/Common/4K/WindowTitlebarMiddle" );

        return textures;
    }

    public List < String > getWanted4k3DDSTextures ( ) {
        List < String > textures = new ArrayList <>(  );

        textures.add( "Art/2DArt/UIImages/Common/4K/ButtonCloseHover" );
        textures.add( "Art/2DArt/UIImages/Common/4K/ButtonCloseDown" );
        textures.add( "Art/2DArt/UIImages/Common/4K/ButtonCloseNormal" );

        textures.add( "Art/2DArt/UIImages/Common/4K/ButtonGreenPressedMiddle" );
        textures.add( "Art/2DArt/UIImages/Common/4K/ButtonGreenPressedLeft" );
        textures.add( "Art/2DArt/UIImages/Common/4K/ButtonGreenPressedRight" );

        textures.add( "Art/2DArt/UIImages/Common/4K/ButtonGreenHoverMiddle" );
        textures.add( "Art/2DArt/UIImages/Common/4K/ButtonGreenHoverLeft" );
        textures.add( "Art/2DArt/UIImages/Common/4K/ButtonGreenHoverRight" );

        textures.add( "Art/2DArt/UIImages/Common/4K/ButtonGreenNormalRight" );
        textures.add( "Art/2DArt/UIImages/Common/4K/ButtonGreenNormalMiddle" );
        textures.add( "Art/2DArt/UIImages/Common/4K/ButtonGreenNormalRight" );

        textures.add( "Art/2DArt/UIImages/Common/4K/ButtonGenericHoverLeft" );
        textures.add( "Art/2DArt/UIImages/Common/4K/ButtonGenericHoverMiddle" );
        textures.add( "Art/2DArt/UIImages/Common/4K/ButtonGenericHoverRight" );

        textures.add( "Art/2DArt/UIImages/Common/4K/ButtonGenericNormalLeft"  );
        textures.add( "Art/2DArt/UIImages/Common/4K/ButtonGenericNormalMiddle" );
        textures.add( "Art/2DArt/UIImages/Common/4K/ButtonGenericNormalRight" );

        textures.add( "Art/2DArt/UIImages/Common/4K/ButtonGenericPressedLeft" );
        textures.add( "Art/2DArt/UIImages/Common/4K/ButtonGenericPressedMiddle" );
        textures.add( "Art/2DArt/UIImages/Common/4K/ButtonGenericPressedRight" );

        textures.add( "Art/2DArt/UIImages/Common/4K/CheckBoxChecked2Middle" );
        textures.add( "Art/2DArt/UIImages/Common/4K/CheckBoxChecked2Right" );
        textures.add( "Art/2DArt/UIImages/Common/4K/CheckBoxChecked2Left" );

        textures.add( "Art/2DArt/UIImages/Common/4K/CheckBoxUnchecked2Right" );
        textures.add( "Art/2DArt/UIImages/Common/4K/CheckBoxUnchecked2Left" );
        textures.add( "Art/2DArt/UIImages/Common/4K/CheckBoxUnchecked2Middle" );

        return textures;
    }

    public List < String > getWanted4k6DDSTextures ( ) {
        List < String > textures = new ArrayList <>(  );

        textures.add( "Art/2DArt/UIImages/InGame/4K/ItemsBackground2x3" );
        textures.add( "Art/2DArt/UIImages/InGame/4K/MicrotransactionsPopUpHeaderBackground" );

        return textures;
    }

    public List < String > getWanted4k11DDSTextures ( ) {
        List < String > textures = new ArrayList <>(  );

        textures.add( "Art/2DArt/UIImages/InGame/4K/Tick" );
        textures.add( "Art/2DArt/UIImages/InGame/4K/Cross" );

        return textures;
    }

    public List < String > getWanted1DDSTextures ( ) {
        List < String > textures = new ArrayList <>(  );

        textures.add( "Art/2DArt/UIImages/InGame/VendorBuy" );
        textures.add( "Art/2DArt/UIImages/InGame/AtlasMissionNotification" );
        textures.add( "Art/2DArt/UIImages/InGame/AtlasWatchstoneBacking" );


        return textures;
    }

    public List < String > getWanted2DDSTextures ( ) {
        List < String > textures = new ArrayList <>(  );

        textures.add( "Art/2DArt/UIImages/InGame/RecipeUnlockedBG" );
        textures.add( "Art/2DArt/UIImages/InGame/RNumber7" );
        textures.add( "Art/2DArt/UIImages/InGame/VendorSell" );

        return textures;
    }

    public List < String > getWanted3DDSTextures ( ) {
        List < String > textures = new ArrayList <>(  );

        textures.add( "Art/2DArt/UIImages/InGame/InvExpandNormal" );
        textures.add( "Art/2DArt/UIImages/InGame/InvExpandHover" );
        textures.add( "Art/2DArt/UIImages/InGame/RNumber8" );

        return textures;
    }

    public List < String > getWanted4DDSTextures ( ) {
        List < String > textures = new ArrayList <>(  );

        textures.add( "Art/2DArt/UIImages/InGame/ApplyChangesButtonFrame" );
        textures.add( "Art/2DArt/UIImages/InGame/AtlasObjectiveBacking" );
        textures.add( "Art/2DArt/UIImages/InGame/UnlockedFavouredMapNotification" );

        return textures;
    }

    public List < String > getWanted5DDSTextures ( ) {
        List < String > textures = new ArrayList <>(  );

        textures.add( "Art/2DArt/UIImages/InGame/InvExpandPressed" );
        textures.add( "Art/2DArt/UIImages/InGame/InvCollapseNormal" );

        return textures;
    }

    public List < String > getWanted7DDSTextures ( ) {
        List < String > textures = new ArrayList <>(  );

        textures.add( "Art/2DArt/UIImages/InGame/InvCollapseHover" );
        textures.add( "Art/2DArt/UIImages/InGame/InvCollapsePressed" );
        textures.add( "Art/2DArt/UIImages/InGame/RNumber9" );
        textures.add( "Art/2DArt/UIImages/InGame/RNumber0" );
        textures.add( "Art/2DArt/UIImages/InGame/RNumber1" );
        textures.add( "Art/2DArt/UIImages/InGame/RNumber2" );
        textures.add( "Art/2DArt/UIImages/InGame/RNumber3" );
        textures.add( "Art/2DArt/UIImages/InGame/RNumber4" );

        return textures;
    }

    public List < String > getWanted10DDSTextures ( ) {
        List < String > textures = new ArrayList <>(  );

        textures.add( "Art/2DArt/UIImages/InGame/Number3" );
        textures.add( "Art/2DArt/UIImages/InGame/WardrobeDeleteOutfitBacking" );
        textures.add( "Art/2DArt/UIImages/InGame/WardrobeSaveOutfitBacking" );

        return textures;
    }

    public List < String > getWanted13DDSTextures ( ) {
        List < String > textures = new ArrayList <>(  );

        textures.add( "Art/2DArt/UIImages/InGame/Number9" );
        textures.add( "Art/2DArt/UIImages/InGame/Number8" );
        textures.add( "Art/2DArt/UIImages/InGame/Number7" );


        return textures;
    }

    public List < String > getWanted15DDSTextures ( ) {
        List < String > textures = new ArrayList <>(  );

        textures.add( "Art/2DArt/UIImages/InGame/Number4" );
        textures.add( "Art/2DArt/UIImages/InGame/Number1" );
        textures.add( "Art/2DArt/UIImages/InGame/Number0" );
        textures.add( "Art/2DArt/UIImages/InGame/MemoryButtonUnavailable" );
        textures.add( "Art/2DArt/UIImages/InGame/MemoryButtonHover" );
        textures.add( "Art/2DArt/UIImages/InGame/MemoryButtonClicked" );
        textures.add( "Art/2DArt/UIImages/InGame/MemoryButtonNormal" );
        textures.add( "Art/2DArt/UIImages/InGame/ButtonStashUnlockPressed" );

        return textures;
    }

    public List < String > getWanted16DDSTextures ( ) {
        List < String > textures = new ArrayList <>(  );

        textures.add( "Art/2DArt/UIImages/InGame/ButtonStashLockHover" );
        textures.add( "Art/2DArt/UIImages/InGame/ButtonStashUnlockHover" );
        textures.add( "Art/2DArt/UIImages/InGame/ButtonStashLockPressed");
        textures.add( "Art/2DArt/UIImages/InGame/ButtonStashLockNormal" );

        return textures;
    }

    public List < String > getWanted17DDSTextures ( ) {
        List < String > textures = new ArrayList <>(  );

        textures.add( "Art/2DArt/UIImages/InGame/WardrobeDeleteOutfitButtonHighlight" );
        textures.add( "Art/2DArt/UIImages/InGame/WardrobeDeleteOutfitButtonDefault" );
        textures.add( "Art/2DArt/UIImages/InGame/WardrobeDeleteOutfitButtonClicked" );

        textures.add( "Art/2DArt/UIImages/InGame/WebFilterButtonDefault" );
        textures.add( "Art/2DArt/UIImages/InGame/WebFilterButtonHighlight" );
        textures.add( "Art/2DArt/UIImages/InGame/WebFilterButtonPressed" );

        return textures;
    }

    public List < String > getWantedM1DDSTextures ( ) {
        List < String > textures = new ArrayList <>(  );

        textures.add( "Art/2DArt/UIImages/InGame/Marketplace/ButtonRedPressedLeft" );
        textures.add( "Art/2DArt/UIImages/InGame/Marketplace/ButtonRedPressedMiddle" );
        textures.add( "Art/2DArt/UIImages/InGame/Marketplace/ButtonRedHoverMiddle" );

        return textures;
    }

    public List < String > getWantedM2DDSTextures ( ) {
        List < String > textures = new ArrayList <>(  );

        textures.add( "Art/2DArt/UIImages/InGame/Marketplace/ButtonRedHoverLeft" );
        textures.add( "Art/2DArt/UIImages/InGame/Marketplace/ButtonRedNormalLeft" );
        textures.add( "Art/2DArt/UIImages/InGame/Marketplace/ButtonRedNormalMiddle" );
        textures.add( "Art/2DArt/UIImages/InGame/Marketplace/ButtonRedNormalRight" );
        textures.add( "Art/2DArt/UIImages/InGame/Marketplace/ButtonRedHoverRight" );

        return textures;
    }
}
