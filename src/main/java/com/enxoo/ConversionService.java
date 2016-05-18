package com.enxoo;

import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Root resource (exposed at "convert" path)
 */
@Path("convert")
public class ConversionService {

    private static final String username = "enxooUserMS";
    private static final String password = "veryhardtorememberpassword";

    @Context
    private HttpServletRequest request;

    /**
     * Method handling HTTP POST requests. The returned object will be sent
     * to the client as "octet_stream" media type.
     *
     * @return Response object
     */
    @POST
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response getIt(InputStream fileInputStream) {

        if(!isAuthorized(request.getHeader("authorization"))){
            return Response.serverError().entity("Authorization failed.").build();
        }

        File pdfResult;
        try {
            pdfResult = new File("pdf" + fileInputStream.hashCode() + ".pdf");
            XWPFDocument document = new XWPFDocument(fileInputStream);
            PdfOptions options = PdfOptions.create();
            OutputStream pdfStream = new FileOutputStream(pdfResult);
            PdfConverter.getInstance().convert(document, pdfStream, options);
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.serverError().entity("Conversion failed...").build();
        }

        return Response.ok().type("application/pdf").entity(pdfResult).build();
    }

    private boolean isAuthorized(String header){

        String decoded;
        try{
            String data = header.substring(header.indexOf(" ") +1 );

            // Decode the data back to original string
            byte[] bytes = new BASE64Decoder().decodeBuffer(data);
            decoded = new String(bytes);

        }catch(Exception e){
            return false;
        }

        String[] credentials = decoded.split(":");

        return (credentials[0].equals(username) && credentials[1].equals(password));
    }

}
