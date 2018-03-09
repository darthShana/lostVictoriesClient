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


def blenderExport(blendFile, outputDir):
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

    BLENDER2OGRE_EXPR = r"""import bpy; bpy.ops.ogre.export(EX_COPY_SHADER_PROGRAMS=True, EX_SWAP_AXIS='xz-y', EX_SEP_MATS=False, EX_ONLY_DEFORMABLE_BONES=False, EX_ONLY_ANIMATED_BONES=False, EX_SCENE=True, EX_SELONLY=False, EX_FORCE_CAMERA=True, EX_FORCE_LAMPS=True, EX_MESH=True, EX_MESH_OVERWRITE=True, EX_ARM_ANIM=True, EX_SHAPE_ANIM=True, EX_INDEPENDENT_ANIM=False, EX_TRIM_BONE_WEIGHTS=0.01, EX_ARRAY=True, EX_MATERIALS=True, EX_FORCE_IMAGE_FORMAT='NONE', EX_DDS_MIPS=1, EX_lodLevels=0, EX_lodDistance=300, EX_lodPercent=40, EX_nuextremityPoints=0, EX_generateEdgeLists=False, EX_generateTangents=True, EX_tangentSemantic='uvw', EX_tangentUseParity=4, EX_tangentSplitMirrored=False, EX_tangentSplitRotated=False, EX_reorganiseBuffers=True, EX_optimiseAnimations=True, filepath='{0}')""".format(exportOutFile.replace('\\', '\\\\'))

    # Because we are running blender without GUI, this call tends to return exit code 11 even when successful.
    # BKE_icon_get: Internal error, no icon for icon ID
    # Check for pass/fail by looking at artifacts instead, assuming they did not exist before.
    subprocess.call([BLENDER, "--background", blendFile, "--python-expr", BLENDER2OGRE_EXPR])

    requiredArtifactSuffices = [".material", ".mesh", ".mesh.xml", ".skeleton", ".skeleton.xml"]
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


def bulkExport():
    """
    For when you want to export multiple files at once.
    erable program or batch file.
        python -c "import exporter; exporter.bulkExport()"
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

    # Actual export work
    outDir = os.path.abspath(outDir)
    successful = []
    failure = []

    for blendFile in blendFiles:
        try:
            blendFilePath = os.path.abspath(blendFile)
            blenderExport(blendFilePath, outDir)
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