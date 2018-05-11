#!/usr/bin/env python

"""
Exports blender file to ogre xml.
"""

from __future__ import print_function
import os
import sys
import argparse
import subprocess
import time

# blender exe path
BLENDER = r"""C:\Program Files\Blender Foundation\Blender\blender.exe""" 

# Default Ogre XML export options to use
DEFAULT_EXPORT_OPTS = {
    "EX_COPY_SHADER_PROGRAMS" : True,
    "EX_SWAP_AXIS" : 'xyz',
    "EX_SEP_MATS" : False,
    "EX_ONLY_DEFORMABLE_BONES" : False,
    "EX_ONLY_ANIMATED_BONES" : False,
    "EX_SCENE" : True,
    "EX_SELONLY" : False,
    "EX_FORCE_CAMERA" : True,
    "EX_FORCE_LAMPS" : True,
    "EX_MESH" : True,
    "EX_MESH_OVERWRITE" : True,
    "EX_ARM_ANIM" : True,
    "EX_SHAPE_ANIM" : True,
    "EX_INDEPENDENT_ANIM" : False,
    "EX_TRIM_BONE_WEIGHTS" : 0.01,
    "EX_ARRAY" : True,
    "EX_MATERIALS" : True,
    "EX_FORCE_IMAGE_FORMAT" : 'NONE',
    "EX_DDS_MIPS" : 1,
    "EX_lodLevels" : 0,
    "EX_lodDistance" : 300,
    "EX_lodPercent" : 40,
    "EX_nuextremityPoints" : 0,
    "EX_generateEdgeLists" : False,
    "EX_generateTangents" : True,
    "EX_tangentSemantic" : 'uvw',
    "EX_tangentUseParity" : 4,
    "EX_tangentSplitMirrored" : False,
    "EX_tangentSplitRotated" : False,
    "EX_reorganiseBuffers" : True,
    "EX_optimiseAnimations" : True,    
}


def getExportOptionString(exportOptions):
    optionsList = []
    for key, value in exportOptions.items():
        # Add quote mark around 'value' if its a string
        if isinstance(value, str):
            value = "'" + value + "'"
        optionsList.append('%s=%s' % (key, value))
    
    return ", ".join(optionsList)


def blenderExport(blendFile, outputDir, opts={}):
    # Get the blender file name without extension
    outFileBase = os.path.splitext(os.path.basename(blendFile))[0]
    outFileBase = os.path.join(outputDir, outFileBase)

    # file name for blend2ogre
    exportSuffix = ".scene"
    exportOutFile = outFileBase + exportSuffix

    print("========== Exporting to Ogre XML [start] ==========")
    print("Input: {0}".format(blendFile))
    print("Output: {0}".format(exportOutFile))
    print("========== Exporting to Ogre XML [start] ==========")
    print()

    # Export options - use defaults, override with opts provided
    exportOptions = dict(DEFAULT_EXPORT_OPTS)
    exportOptions.update(opts)
    # Add in the file to export
    exportOptions["filepath"] = exportOutFile.replace('\\', '\\\\')
    exportOptionsStr = getExportOptionString(exportOptions)

    BLENDER2OGRE_EXPR = r"""import bpy; bpy.ops.ogre.export({0})""".format(exportOptionsStr)

    # Because we are running blender without GUI, this call tends to return exit code 11 even when successful.
    # BKE_icon_get: Internal error, no icon for icon ID
    # Check for pass/fail by looking at artifacts instead, assuming they did not exist before.
    subprocess.call([BLENDER, "--background", blendFile, "--python-expr", BLENDER2OGRE_EXPR])

    requiredArtifactSuffices = [".material", ".mesh", ".mesh.xml"]
    artifactSuffices = [".scene"].extend(requiredArtifactSuffices)

    for aSfx in requiredArtifactSuffices:
        artifactPath = outFileBase + aSfx
        if not os.path.exists(artifactPath):
            raise RuntimeError("Ogre artifact {0} does not exist.".format(artifactPath))

    # No exception means we have all the artifacts required.

    print()
    print("========== Exporting to Ogre XML [end] ==========")
    print("Input: {0}".format(blendFile))
    print("Output: {0}".format(exportOutFile))
    print("========== Exporting to Ogre XML [end] ==========")
    print()


def bulkExportSoldiers():
    """
    For when you want to export multiple files at once.
        python -c "import exporter; exporter.bulkExportSoldiers()"
    @return True if there are no failures
    """
    
    # Specify list of input files
    blendFiles = [
        "Soldier/American_Colonel.blend",
        "Soldier/American_Corporal.blend",
        "Soldier/American_Lieutenant.blend",
        "Soldier/American_Private.blend",
        "Soldier/German_Colonel.blend",
        "Soldier/German_Corporal.blend",
        "Soldier/German_Lieutenant.blend",
        "Soldier/German_Private.blend"
    ]

    # Specify output directory
    outDir = "Characters"

    return bulkExport(blendFiles, outDir, {"EX_SWAP_AXIS" : 'xz-y'})


def bulkExportHouses():
    """
    For when you want to export multiple files at once.
        python -c "import exporter; exporter.bulkExportHouses()"
    @return True if there are no failures
    """
    
    # Specify list of input files
    blendFiles = [
        "Structures/americanFlag.blend",
        "Structures/bridge_short.blend",
        "Structures/Bunker.blend",
        "Structures/casaMedieval.blend",
        "Structures/Chapel.blend",
        "Structures/church3.blend",
        "Structures/cottage.blend",
        "Structures/fountain1.blend",
        "Structures/germanFlag.blend",
        "Structures/house.blend",
        "Structures/house_1.blend",
        "Structures/house2.blend",
        "Structures/lowpolycart.blend",
        "Structures/market.blend",
        # "Structures/neutralFlag.blend",
        "Structures/ponteBridge.blend",
        "Structures/school.blend",
        "Structures/shed.blend",
        "Structures/stable.blend",
        "Structures/target.blend",
        "Structures/tavern.blend",
        "Structures/watchtower.blend",
        "Structures/WaterPoweredSawmill.blend",
        "Structures/Well.blend",
        "Structures/Windmill.blend"
    ]

    # Specify output directory
    outDir = "Structures"

    return bulkExport(blendFiles, outDir, {"EX_SWAP_AXIS" : 'xz-y'})


def bulkExport(blendFiles, outputDir, opts={}):
    """
    Actual export work
    @return True if there are no failures
    """
    outDir = os.path.abspath(outputDir)
    successful = []
    failure = []

    for blendFile in blendFiles:
        try:
            blendFilePath = os.path.abspath(blendFile)
            blenderExport(blendFilePath, outDir, opts=opts)
            successful.append(blendFile)
            time.sleep(1)
        except RuntimeError:
            failure.append(blendFile)

    print("========== Successful Conversions ==========")
    print("\n".join(successful))
    print()
    print("========== Failed Conversions ==========")
    print("\n".join(failure))
    print()

    return not failure


def main(arguments):
    parser = argparse.ArgumentParser(
        description=__doc__,
        formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument('infile', help="Input blender file")
    parser.add_argument('outdir', help="Output directory")

    args = parser.parse_args(arguments)

    # Check files are good
    blendFilePath = os.path.abspath(args.infile)
    if not os.path.exists(blendFilePath):
        raise RuntimeError("Blender file {0} does not exist.".format(blendFilePath))

    outputDir = os.path.abspath(args.outdir)

    # Start by exporting to ogre xml
    blenderExport(blendFilePath, outputDir)


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))