package cam.pele.sgm.datagen;

import cam.pele.sgm.config.SgmConfigManager;
import cam.pele.sgm.config.model.SgmConfig;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonParser;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class SchemaProvider implements DataProvider {
    private final PackOutput output;

    public SchemaProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        // 1. Configure the generator
        // GsonModule is not available on Maven Central, manual config required
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2019_09,
                OptionPreset.PLAIN_JSON);

        // Handle @SerializedName manually
        configBuilder.forFields().withPropertyNameOverrideResolver(field -> {
            com.google.gson.annotations.SerializedName annotation = field
                    .getAnnotationConsideringFieldAndGetter(com.google.gson.annotations.SerializedName.class);
            return annotation != null ? annotation.value() : null;
        });

        // Options to make schema cleaner
        configBuilder.with(Option.DEFINITIONS_FOR_ALL_OBJECTS);
        configBuilder.with(Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT);

        // Important: Ignore "$schema" field during generation to avoid useless
        // recursion
        configBuilder.forFields()
                .withIgnoreCheck(field -> field.getName().equals("schema")
                        && field.getDeclaringType().getErasedType() == SgmConfig.class);

        SchemaGenerator generator = new SchemaGenerator(configBuilder.build());

        // 2. Generate JsonNode for root class SgmConfig
        JsonNode jsonSchema = generator.generateSchema(SgmConfig.class);

        // 3. Add title and description
        if (jsonSchema.isObject()) {
            ((ObjectNode) jsonSchema).put("title", "SGM Configuration Info");
            ((ObjectNode) jsonSchema).put("description",
                    "Hover over properties to see details.");
        }

        // 4. Set output path
        Path outputPath = this.output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve(SgmConfigManager.SCHEMA_FILENAME);

        // 5. Save file
        // Convert Jackson JsonNode to GSON JsonElement to use Minecraft's saveStable
        return DataProvider.saveStable(cache, JsonParser.parseString(jsonSchema.toString()), outputPath);
    }

    @Override
    public String getName() {
        return "SGM Config Schema Generator";
    }
}
