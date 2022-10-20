
package ae.adpolice.gov.wbc.tables;

import com.vasco.digipass.sdk.utils.utilities.UtilitiesSDK;
import com.vasco.digipass.sdk.utils.utilities.UtilitiesSDKConstants;
import com.vasco.digipass.sdk.utils.wbc.MzdMatrix;
import com.vasco.digipass.sdk.utils.wbc.SerializationR;
import com.vasco.digipass.sdk.utils.wbc.WBCSDKTables;
import com.vasco.digipass.sdk.utils.wbc.WbAesUtils;

/**
 * Accesses the whitebox encryption tables
 */
public class WBCSDKTablesImpl implements WBCSDKTables
{
    private static MzdMatrix initial_encoding;

    private static MzdMatrix final_decoding;

    private static byte[][][][] typeIA_input_sbox;

    private static byte[][][][] typeIAs;

    private static byte[][][][][] typeIV_IAs;

    private static byte[][][][][] typeIIs;

    private static byte[][][][][][] typeIV_IIs;

    private static byte[][][][][] typeIIIs;

    private static byte[][][][][][] typeIV_IIIs;

    private static byte[][][][] typeIBs;

    private static byte[][][][][] typeIV_IBs;

    private static byte[][][][] typeIB_output_sbox_inv;

    public MzdMatrix getInitialEncoding()
    {
        if (initial_encoding == null)
        {
            byte[] tmp = new byte[UtilitiesSDKConstants.INITIAL_ENCODING_LENGTH];

            int offset = 0;
            offset += UtilitiesSDK.hexaToBytes(Wb_initial_encoding_enc.Data01, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_initial_encoding_enc.Data02, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_initial_encoding_enc.Data03, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_initial_encoding_enc.Data04, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_initial_encoding_enc.Data05, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_initial_encoding_enc.Data06, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_initial_encoding_enc.Data07, tmp, offset);

            initial_encoding = SerializationR.message_to_gf2matrix(tmp);
        }

        return initial_encoding;
    }

    public MzdMatrix getFinalDecoding()
    {
        if (final_decoding == null)
        {
            byte[] tmp = new byte[UtilitiesSDKConstants.FINAL_DECODING_LENGTH];

            int offset = 0;
            offset += UtilitiesSDK.hexaToBytes(Wb_final_decoding_enc.Data01, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_final_decoding_enc.Data02, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_final_decoding_enc.Data03, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_final_decoding_enc.Data04, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_final_decoding_enc.Data05, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_final_decoding_enc.Data06, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_final_decoding_enc.Data07, tmp, offset);

            final_decoding = SerializationR.message_to_gf2matrix(tmp);
        }

        return final_decoding;
    }

    public byte[][][][] getTypeIA_input_sbox()
    {
        if (typeIA_input_sbox == null)
        {
            byte[] tmp = UtilitiesSDK.hexaToBytes(Wb_typeIA_input_sbox_enc.Data01);

            typeIA_input_sbox = WbAesUtils.init_typeIA_input_sbox();
            SerializationR.message_to_typeIA_input_sbox(tmp, typeIA_input_sbox);
        }

        return typeIA_input_sbox;
    }

    public byte[][][][] getTypeIAs()
    {
        if (typeIAs == null)
        {
            byte[] tmp = new byte[UtilitiesSDKConstants.TYPE_IAS_LENGTH];

            int offset = 0;
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIAs_enc.Data01, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIAs_enc.Data02, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIAs_enc.Data03, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIAs_enc.Data04, tmp, offset);

            typeIAs = WbAesUtils.init_typeIAs();
            SerializationR.message_to_typeIAs(tmp, typeIAs);
        }

        return typeIAs;
    }

    public byte[][][][][] getTypeIV_IAs()
    {
        if (typeIV_IAs == null)
        {
            byte[] tmp = new byte[UtilitiesSDKConstants.TYPE_IV_IAS_LENGTH];

            int offset = 0;
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIV_IAs_enc.Data01, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIV_IAs_enc.Data02, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIV_IAs_enc.Data03, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIV_IAs_enc.Data04, tmp, offset);

            typeIV_IAs = WbAesUtils.init_typeIV_IAs();
            SerializationR.message_to_typeIV_IAs(tmp, typeIV_IAs);
        }

        return typeIV_IAs;
    }

    public byte[][][][][] getTypeIIs()
    {
        if (typeIIs == null)
        {
            byte[] tmp = new byte[UtilitiesSDKConstants.TYPE_IIS_LENGTH];

            int offset = 0;
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIIs_enc.Data01, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIIs_enc.Data02, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIIs_enc.Data03, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIIs_enc.Data04, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIIs_enc.Data05, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIIs_enc.Data06, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIIs_enc.Data07, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIIs_enc.Data08, tmp, offset);

            typeIIs = WbAesUtils.init_typeIIs();
            SerializationR.message_to_typeIIs(tmp, typeIIs);
        }

        return typeIIs;
    }

    public byte[][][][][][] getTypeIV_IIs()
    {
        if (typeIV_IIs == null)
        {
            byte[] tmp = new byte[UtilitiesSDKConstants.TYPE_IV_IIS_LENGTH];

            int offset = 0;
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIV_IIs_enc.Data01, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIV_IIs_enc.Data02, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIV_IIs_enc.Data03, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIV_IIs_enc.Data04, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIV_IIs_enc.Data05, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIV_IIs_enc.Data06, tmp, offset);

            typeIV_IIs = WbAesUtils.init_typeIV_IIs();
            SerializationR.message_to_typeIV_IIs(tmp, typeIV_IIs);
        }

        return typeIV_IIs;
    }

    public byte[][][][][] getTypeIIIs()
    {
        if (typeIIIs == null)
        {
            byte[] tmp = new byte[UtilitiesSDKConstants.TYPE_IIIS_LENGTH];

            int offset = 0;
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIIIs_enc.Data01, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIIIs_enc.Data02, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIIIs_enc.Data03, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIIIs_enc.Data04, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIIIs_enc.Data05, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIIIs_enc.Data06, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIIIs_enc.Data07, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIIIs_enc.Data08, tmp, offset);

            typeIIIs = WbAesUtils.init_typeIIIs();
            SerializationR.message_to_typeIIIs(tmp, typeIIIs);
        }

        return typeIIIs;
    }

    public byte[][][][][][] getTypeIV_IIIs()
    {
        if (typeIV_IIIs == null)
        {
            byte[] tmp = new byte[UtilitiesSDKConstants.TYPE_IV_IIIS_LENGTH];

            int offset = 0;
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIV_IIIs_enc.Data01, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIV_IIIs_enc.Data02, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIV_IIIs_enc.Data03, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIV_IIIs_enc.Data04, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIV_IIIs_enc.Data05, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIV_IIIs_enc.Data06, tmp, offset);

            typeIV_IIIs = WbAesUtils.init_typeIV_IIIs();
            SerializationR.message_to_typeIV_IIIs(tmp, typeIV_IIIs);
        }

        return typeIV_IIIs;
    }

    public byte[][][][] getTypeIBs()
    {
        if (typeIBs == null)
        {
            byte[] tmp = new byte[UtilitiesSDKConstants.TYPE_IBS_LENGTH];

            int offset = 0;
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIBs_enc.Data01, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIBs_enc.Data02, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIBs_enc.Data03, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIBs_enc.Data04, tmp, offset);

            typeIBs = WbAesUtils.init_typeIBs();
            SerializationR.message_to_typeIBs(tmp, typeIBs);
        }

        return typeIBs;
    }

    public byte[][][][][] getTypeIV_IBs()
    {
        if (typeIV_IBs == null)
        {
            byte[] tmp = new byte[UtilitiesSDKConstants.TYPE_IV_IBS_LENGTH];

            int offset = 0;
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIV_IBs_enc.Data01, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIV_IBs_enc.Data02, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIV_IBs_enc.Data03, tmp, offset);
            offset += UtilitiesSDK.hexaToBytes(Wb_typeIV_IBs_enc.Data04, tmp, offset);

            typeIV_IBs = WbAesUtils.init_typeIV_IBs();
            SerializationR.message_to_typeIV_IBs(tmp, typeIV_IBs);
        }

        return typeIV_IBs;
    }

    public byte[][][][] getTypeIB_output_sbox_inv()
    {
        if (typeIB_output_sbox_inv == null)
        {
            byte[] tmp = UtilitiesSDK.hexaToBytes(Wb_typeIB_output_sbox_inv_enc.Data01);

            typeIB_output_sbox_inv = WbAesUtils.init_typeIB_output_sbox_inv();
            SerializationR.message_to_typeIB_output_sbox_inv(tmp, typeIB_output_sbox_inv);
        }

        return typeIB_output_sbox_inv;
    }
}
