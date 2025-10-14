package org.example;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final PresentationTools presentationTools = new PresentationTools();

    static void main(String[] args) {

        // Stdio Server Transport (Support for SSE also available)
        var transportProvider = new StdioServerTransportProvider(new ObjectMapper());
        // Sync tool specification
//        var syncToolSpecification = getSyncToolSpecification();

        // Create a server with custom configuration
        McpSyncServer syncServer = McpServer.sync(transportProvider)
                .serverInfo("javaone-mcp-server", "0.0.1")
                .capabilities(McpSchema.ServerCapabilities.builder()
                        .tools(true)
                        .logging()
                        .build())
                // Register tools, resources, and prompts
                .tools(getSyncToolSpecifications())
                .build();

        log.info("Starting JavaOne MCP Server...");
    }

    private static List<McpServerFeatures.SyncToolSpecification> getSyncToolSpecifications() {
        var allSchema = """
        {
          "type" : "object",
          "id" : "urn:jsonschema:Operation",
          "properties" : {
            "operation" : {
              "type" : "string"
            }
          }
        }
        """;
        var byYearSchema = """
        {
          "type" : "object",
          "id" : "urn:jsonschema:PresentationByYear",
          "properties" : {
            "year" : {
              "type" : "integer"
            }
          },
          "required": ["year"]
        }
        """;
        var getAllTool = new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("get_presentations", "Get a list of all presentations from JavaOne", allSchema),
                (exchange, arguments) -> {
                    List<Presentation> presentations = presentationTools.getPresentations();
                    List<McpSchema.Content> contents = new ArrayList<>();
                    for (Presentation presentation : presentations) {
                        contents.add(new McpSchema.TextContent(presentation.toString()));
                    }
                    return new McpSchema.CallToolResult(contents, false);
                }
        );
        var getByYearTool = new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("get_presentations_by_year", "Get presentations by year", byYearSchema),
                (exchange, arguments) -> {
                    int year = (int) arguments.get("year");
                    List<Presentation> presentations = presentationTools.getPresentationsByYear(year);
                    List<McpSchema.Content> contents = new ArrayList<>();
                    for (Presentation presentation : presentations) {
                        contents.add(new McpSchema.TextContent(presentation.toString()));
                    }
                    return new McpSchema.CallToolResult(contents, false);
                }
        );
        return List.of(getAllTool, getByYearTool);
    }


}
