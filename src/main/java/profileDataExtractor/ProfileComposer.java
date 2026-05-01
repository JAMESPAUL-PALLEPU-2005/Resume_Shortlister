package profileDataExtractor;

import java.util.ArrayList;

public class ProfileComposer implements DataExtractor {
    private ArrayList<DataExtractor> dataExtractors;

    public ProfileComposer(ArrayList<DataExtractor> dataExtractors) {
        this.dataExtractors = dataExtractors;
    }

    @Override
    public String getDataType() {
        return "Profile";
    }

    @Override
    public String getData(String documentText) {
        StringBuilder builder = new StringBuilder();
        for (DataExtractor e : dataExtractors) {
            builder.append(e.getDataType()).append(" : ").append(e.getData(documentText)).append("\n");
        }
        return builder.toString();
    }
}
