package fr.inria.edelweiss.kgramserver.webservice;

import com.sun.jersey.multipart.FormDataBodyPart;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 23 juin 2015
 */
public class Utility {
    
    /**
     * Change a list of <FormDataBodyPart> to <String>
     * @param bodyPartList
     * @return 
     */
    public static List<String> toStringList(List<FormDataBodyPart> bodyPartList) {
        if (bodyPartList == null) {
            return null;
        }

        List<String> stringList = new ArrayList<String>();
        for (FormDataBodyPart fdbp : bodyPartList) {
            stringList.add(fdbp.getValueAs(String.class));
        }
        return stringList;
    }

    /**
     * Read a file to stringbuilder
     * @param path
     * @return
     * @throws IOException 
     */
    public static StringBuilder readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new StringBuilder(new String(encoded, StandardCharsets.UTF_8));
    }
}
