package de.tomalbrc.filament.util;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;

import java.nio.charset.StandardCharsets;

public class FilamentShaderUtil {

    static String fsh =
            """
             #version 150

             #moj_import <fog.glsl>

             uniform sampler2D Sampler0;

             uniform vec4 ColorModulator;
             uniform float FogStart;
             uniform float FogEnd;
             uniform vec4 FogColor;

             in float vertexDistance;
             in vec4 vertexColor;
             in vec4 lightColor;
             in vec4 faceLightColor;
             in vec2 texCoord0;
             in vec2 texCoord1;
             in vec4 normal;

             out vec4 fragColor;

             vec4 apply_partial_emissivity(vec4 inputColor, vec4 originalLightColor, vec3 minimumLightColor) {
                 vec4 newLightColor = originalLightColor;
                 newLightColor.r = max(originalLightColor.r, minimumLightColor.r);
                 newLightColor.g = max(originalLightColor.g, minimumLightColor.g);
                 newLightColor.b = max(originalLightColor.b, minimumLightColor.b);
                 return inputColor * newLightColor;
             }

             // 250=partial emissive + faceLight, 251=just partial, 252 full emissive
             vec4 make_emissive(vec4 inputColor, vec4 lightColor, vec4 faceLightColor, int inputAlpha) {

                 if(inputAlpha != 252 && inputAlpha != 251) inputColor *= faceLightColor;
                
                 if (inputAlpha >= 250 && inputAlpha <= 252) inputColor.a = 1.0;

                 if (inputAlpha == 252) return inputColor;
                 if (inputAlpha == 251 || inputAlpha == 250) return apply_partial_emissivity(inputColor, lightColor, vec3(0.7));
                
                 return inputColor * lightColor;
             }

             void main() {
                 vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
                 int alpha = int(round(textureLod(Sampler0, texCoord0, 0.0).a * 255.0));
                 color = make_emissive(color, lightColor, faceLightColor, alpha);
                 if(color.a < 0.1) discard;
                 fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
             }

             """;

    static String vsh =
            """
            #version 150
                        
            #moj_import <light.glsl>
            #moj_import <fog.glsl>
                        
            in vec3 Position;
            in vec4 Color;
            in vec2 UV0;
            in vec2 UV1;
            in ivec2 UV2;
            in vec3 Normal;
                        
            uniform sampler2D Sampler2;
                        
            uniform mat4 ModelViewMat;
            uniform mat4 ProjMat;
            uniform int FogShape;
            uniform mat3 IViewRotMat;
                        
            uniform vec3 Light0_Direction;
            uniform vec3 Light1_Direction;
                        
            out float vertexDistance;
            out vec4 vertexColor;
            out vec4 lightColor;
            out vec4 faceLightColor;
            out vec2 texCoord0;
            out vec2 texCoord1;
            out vec2 texCoord2;
            out vec4 normal;
                        
            void main() {
                gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
                        
                vertexDistance = fog_distance(ModelViewMat, IViewRotMat * Position, FogShape);
                vertexColor = Color;
                lightColor = minecraft_sample_lightmap(Sampler2, UV2);
                faceLightColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, vec4(1.0));
                texCoord0 = UV0;
                texCoord1 = UV1;
                texCoord2 = UV2;
                normal = ProjMat * ModelViewMat * vec4(Normal, 0.0);
            }
            """;

    public static void registerCallback() {
        PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(resourcePackBuilder -> {
            resourcePackBuilder.addData("assets/minecraft/shaders/core/rendertype_entity_translucent_cull.fsh", fsh.getBytes(StandardCharsets.UTF_8));
            resourcePackBuilder.addData("assets/minecraft/shaders/core/rendertype_entity_translucent_cull.vsh", vsh.getBytes(StandardCharsets.UTF_8));
        });
    }

}
